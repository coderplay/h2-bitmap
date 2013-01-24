/*
 * Copyright 1999-2013 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.garuda.plan.logical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.garuda.parser.ast.statement.GarudaSelectQueryBlock;
import com.alibaba.garuda.parser.visitor.GarudaASTVisitorAdapter;
import com.alibaba.garuda.plan.logical.expression.LogicalExpressionPlan;
import com.alibaba.garuda.plan.logical.relational.LOFilter;
import com.alibaba.garuda.plan.logical.relational.LOGroupBy;
import com.alibaba.garuda.plan.logical.relational.LOJoin;
import com.alibaba.garuda.plan.logical.relational.LOLimit;
import com.alibaba.garuda.plan.logical.relational.LOOrderBy;
import com.alibaba.garuda.plan.logical.relational.LOProject;
import com.alibaba.garuda.plan.logical.relational.LOTable;
import com.alibaba.garuda.plan.logical.relational.LogicalRelationalOperator;

/**
 * Generator for creating logical execution plan from SQL parse tree
 * 
 * @author Min Zhou (coderplay@gmail.com)
 */
public class LogicalPlanGenerator extends GarudaASTVisitorAdapter {
    private static final Log LOG = LogFactory.getLog(LogicalPlanGenerator.class);

    private LogicalPlan lp = new LogicalPlan();

    private ProcessingState procState = new ProcessingState(null, null, 0);
    
    // used to find the most recently set LogicalRelationalOperator 
    private LogicalRelationalOperator lOpCache = null;
    
    
    /**
     * A structure containing data which will be needed by
     * LogicalPlanCreator through its processing
     */
    static class ProcessingState {
        
        /**
         * A structure to store multi query optimization
         * specific state required by LogicalPlanCreator during
         * its execution
         */
        static class MultiQuery {
            // For multiquery optimization, we want to reuse the operators
            // representing the "same table" in the different queries. The table
            // here encapsulates an entity representing the same input data
            // (i.e. going aginst the same SQL table with the same where conditions
            // for the partition columns). As we visit a Table in this visitor
            // we update the list - tableToLeafLogOpMapping - to add a map between
            // the table visit and the leaf LogicalOperator it got translated into
            // So if we later visit an "equivalent" table (as determined by
            // table.isEquivalentTo() ), then we will just reuse the leaf logical
            // operator and connect the next operator to it (by setting prevRelationalOp
            // and lOpCache to it).
            List<TableOperatorMapping> tableOperatorMappings;
            
            // a map of tables that were replaced
            // during multi query optimization because a table
            // was found to be equivalent (as determined by
            // Table.isEquivalentTo()) to another.
            // This is used while visiting ColName to resolve 
            // the column by first checking in the table in 
            // the ColName and if it cannot be resolved, by using
            // the replaced table instead
            Map<SQLTableSource, SQLTableSource> replacedTableMap = new HashMap<SQLTableSource, SQLTableSource>();
            
            // This is used to lookup a table using
            // an alias
//            LogicalTableStore lTableStore;
            
            // As we process a query this stack
            // will be used to cache the join as we visit it - this will
            // be used when we visit the left and right tables of a join
            // to ensure that we do not replace left and right tables of 
            // the join with the same equivalent table for the following reason
            // XXX: FIXME:
            // currently in the logical plan, the ImplicitSplitInserter does not introduce splits
            // correctly between two operator with multiple connections like in the 
            // self join case:
            //   Load
            //   |  |
            //   Cogroup
            // Fixing this in ImplicitSplitInserter will also require fixes in LOCogroup
            // to have it inner plans mapped by input number rather than input operator
            // (since in the self join case, the two input operators would refer to the
            // same operator in a graph like above) - We should fix this at some point
            // For now, we will load the same table twice so that the graph
            // looks like:
            // Load  Load
            //  \    /
            //   Cogroup
//            private Stack<Join> joinStack = new Stack<Join>();
//            
//            void addJoin(Join j) {
//                joinStack.push(j);
//            }
//            
//            Join getCurrentJoin() {
//               Join j = null;
//               try{
//                j = joinStack.peek();
//               } catch(EmptyStackException e) {
//                   j = null;
//               }
//               return j;
//            }
//            
//            void removeJoin(Join j) {
//                joinStack.pop();
//            }
        }
        
        
        /**
         * class to encapsulate table to
         * logical operator (leaf in cases where the
         * table is translated into a subgraph like in
         * join, union) mapping
         */
        static class TableOperatorMapping {
            
            SQLTableSource table;
            LogicalRelationalOperator operator;
            int queryId;
            /**
             * @param table
             * @param operator
             * @param queryId
             */
            public TableOperatorMapping(SQLTableSource table,
                    LogicalRelationalOperator operator, int queryId) {
                super();
                this.table = table;
                this.operator = operator;
                this.queryId = queryId;
            }
            
        }
        
        MultiQuery multiQuery;
        SQLSelect select;
        // used to keep track of previous Relational Operator
        // it is static because it will be used by inner plans also
        LogicalRelationalOperator prevRelationalOp;
        int currentQueryId;
//        SymbolTable symTab;
        /**
         * @param tableOperatorMappings
         * @param tableStore
         */
        public ProcessingState(
                List<TableOperatorMapping> tableOperatorMappings,
//                LogicalTableStore tableStore,
                SQLSelect select,
                int queryId
                // int queryId,
                //SymbolTable symTab
                ) {
            this.multiQuery = new MultiQuery();
            this.multiQuery.tableOperatorMappings = tableOperatorMappings;
//            this.multiQuery.lTableStore = tableStore;
            this.select = select;
            this.currentQueryId = queryId;
//            this.symTab = symTab;
        }
        
        public void addTableOpMapping(SQLTableSource t, LogicalRelationalOperator op) {
            multiQuery.tableOperatorMappings.add(new TableOperatorMapping(
                    t, op, currentQueryId));
        }

    }

    @Override
    public boolean visit(SQLSelect x) {
        x.getQuery().setParent(x);

        if (x.getWithSubQuery() != null) {
            x.getWithSubQuery().accept(this);
        }

        x.getQuery().accept(this);

        return false;
    }

    @Override
    public boolean visit(GarudaSelectQueryBlock x) {
        if (SQLSetQuantifier.ALL == x.getDistionOption()) {

        } else if (SQLSetQuantifier.DISTINCT == x.getDistionOption()) {

        } else if (SQLSetQuantifier.UNIQUE == x.getDistionOption()) {

        }

        // FROM clause
        if (x.getFrom() != null) {
            x.getFrom().accept(this);
            connectRelationalOp();
        }

        // WHERE clause
        if (x.getWhere() != null) {
            // x.getWhere().setParent(x);
            createFilterOp(x.getWhere());
            connectRelationalOp();
        }

        // Projection
        if (x.getSelectList() != null) {
            createProjectionOp(x.getSelectList());
            connectRelationalOp();
        }

        // GroupBy clause
        if (x.getGroupBy() != null) {
            x.getGroupBy().accept(this);
            connectRelationalOp();
        }

        // OrderBy clause
        if (x.getOrderBy() != null) {
            x.getOrderBy().accept(this);
            connectRelationalOp();
        }

        // Limit clause
        if (x.getLimit() != null) {
            x.getLimit().accept(this);
            connectRelationalOp();
        }

        return false;
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
//        x.getExpr().accept(this);
//
//        if (x.getAlias() != null) {
//
//        }
        procState.prevRelationalOp = null;
        LogicalRelationalOperator t = new LOTable(lp);
        addToLogicalPlan(t);
        procState.prevRelationalOp = lOpCache;
        return false;
    }
    
    @Override
    public boolean visit(SQLJoinTableSource x) {
        // store the fact that we are visiting this
        // join - this will be used when we visit the
        // left and right tables
//        this.procState.multiQuery.addJoin(x);

//        //check if we can re-use any equivalent table
//        if(findEquivalentTableAndReplace(x))
//            return;

        x.getLeft().accept(this);
        connectRelationalOp();
        LogicalRelationalOperator ltRelOp = procState.prevRelationalOp;
        // store table mapping for left table
//        procState.addTableOpMapping(x.getLeft(), ltRelOp);
        
        x.getRight().accept(this);
        connectRelationalOp();
        LogicalRelationalOperator rtRelOp = procState.prevRelationalOp;
        // store table mapping for right table
//        procState.addTableOpMapping(x.getRight(), rtRelOp);
        
        LOJoin loJoin = new LOJoin(lp);
        addToLogicalPlan(loJoin);
        connectRelationalOp(ltRelOp);
        connectRelationalOp(rtRelOp);
        
        return false;
    }


    @Override
    public boolean visit(SQLSelectGroupByClause x) {
        LogicalRelationalOperator groupbyOp = new LOGroupBy(lp);
        addToLogicalPlan(groupbyOp);
        return false;
    }

    @Override
    public boolean visit(SQLOrderBy x) {
        LogicalRelationalOperator orderbyOp = new LOOrderBy(lp);
        addToLogicalPlan(orderbyOp);
        return false;
    }

    @Override
    public boolean visit(GarudaSelectQueryBlock.Limit limit) {
        LogicalRelationalOperator limitOp = new LOLimit(lp);
        addToLogicalPlan(limitOp);
        return false;
    }
    
    public LogicalPlan getLogicalPlan() {
        return lp;
    }

    private void createFilterOp(SQLExpr expr) {
        if (expr == null)
            return;

        LogicalExpressionPlanGenerator g = new LogicalExpressionPlanGenerator();
        expr.accept(g);
        LogicalRelationalOperator filterOp = new LOFilter(lp, g.getPlan());
        addToLogicalPlan(filterOp);
    }

    private void createProjectionOp(List<SQLSelectItem> items) {
        LogicalRelationalOperator projectionOp = new LOProject(lp);
        addToLogicalPlan(projectionOp);
    }

    private void addToLogicalPlan(LogicalRelationalOperator lo) {
        lp.add(lo);

        // used to find the most recently set LogicalRelationalOperator
        lOpCache = lo;
    }

    private void connectRelationalOp() {
        connectRelationalOp(procState.prevRelationalOp);
    }

    private void connectRelationalOp(LogicalRelationalOperator inputRelOp) {
        procState.prevRelationalOp = lOpCache;
        if (inputRelOp == null || lOpCache == inputRelOp) {
            // lOpCache is not pointing to a new LogicalRelationalOperator
            // the sql clause must have been empty
            // ie nothing to be done here
            return;
        }
        lp.connect(inputRelOp, lOpCache);
    }


}
