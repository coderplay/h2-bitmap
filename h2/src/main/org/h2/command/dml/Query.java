/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.dml;

import java.util.ArrayList;
import java.util.HashSet;
import org.h2.command.Prepared;
import org.h2.constant.ErrorCode;
import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.expression.Alias;
import org.h2.expression.Expression;
import org.h2.expression.ExpressionColumn;
import org.h2.expression.ExpressionVisitor;
import org.h2.expression.Parameter;
import org.h2.expression.ValueExpression;
import org.h2.message.DbException;
import org.h2.result.LocalResult;
import org.h2.result.ResultTarget;
import org.h2.result.SortOrder;
import org.h2.table.ColumnResolver;
import org.h2.table.Table;
import org.h2.table.TableFilter;
import org.h2.util.New;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueNull;

/**
 * Represents a SELECT statement (simple, or union).
 */
public abstract class Query extends Prepared {

    /**
     * The limit expression as specified in the LIMIT or TOP clause.
     */
    protected Expression limitExpr;

    /**
     * The offset expression as specified in the LIMIT .. OFFSET clause.
     */
    protected Expression offsetExpr;

    /**
     * The sample size
     */
    protected int sampleSize;

    private int lastLimit;
    private long lastEvaluated;
    private LocalResult lastResult;
    private Value[] lastParameters;
    private boolean cacheableChecked, cacheable;

    Query(Session session) {
        super(session);
    }

    /**
     * Execute the query without checking the cache. If a target is specified,
     * the results are written to it, and the method returns null. If no target
     * is specified, a new LocalResult is created and returned.
     *
     * @param limit the limit as specified in the JDBC method call
     * @param target the target to write results to
     * @return the result
     */
    protected abstract LocalResult queryWithoutCache(int limit, ResultTarget target);

    /**
     * Initialize the query.
     */
    public abstract void init();

    /**
     * The the list of select expressions.
     * This may include invisible expressions such as order by expressions.
     *
     * @return the list of expressions
     */
    public abstract ArrayList<Expression> getExpressions();

    /**
     * Calculate the cost to execute this query.
     *
     * @return the cost
     */
    public abstract double getCost();

    /**
     * Get all tables that are involved in this query.
     *
     * @return the set of tables
     */
    public abstract HashSet<Table> getTables();

    /**
     * Set the order by list.
     *
     * @param order the order by list
     */
    public abstract void setOrder(ArrayList<SelectOrderBy> order);

    /**
     * Set the 'for update' flag.
     *
     * @param forUpdate the new setting
     */
    public abstract void setForUpdate(boolean forUpdate);

    /**
     * Get the column count of this query.
     *
     * @return the column count
     */
    public abstract int getColumnCount();

    /**
     * Map the columns to the given column resolver.
     *
     * @param resolver
     *            the resolver
     * @param level
     *            the subquery level (0 is the top level query, 1 is the first
     *            subquery level)
     */
    public abstract void mapColumns(ColumnResolver resolver, int level);

    /**
     * Change the evaluatable flag. This is used when building the execution plan.
     *
     * @param tableFilter the table filter
     * @param b the new value
     */
    public abstract void setEvaluatable(TableFilter tableFilter, boolean b);

    /**
     * Add a condition to the query. This is used for views.
     *
     * @param param the parameter
     * @param columnId the column index (0 meaning the first column)
     * @param comparisonType the comparison type
     */
    public abstract void addGlobalCondition(Parameter param, int columnId, int comparisonType);

    /**
     * Set the distinct flag.
     *
     * @param b the new value
     */
    public abstract void setDistinct(boolean b);

    /**
     * Check if this expression and all sub-expressions can fulfill a criteria.
     * If any part returns false, the result is false.
     *
     * @param visitor the visitor
     * @return if the criteria can be fulfilled
     */
    public abstract boolean isEverything(ExpressionVisitor visitor);

    /**
     * Update all aggregate function values.
     *
     * @param s the session
     */
    public abstract void updateAggregate(Session s);

    /**
     * Call the before triggers on all tables.
     */
    public abstract void fireBeforeSelectTriggers();

    public boolean isQuery() {
        return true;
    }

    public boolean isTransactional() {
        return true;
    }

    private boolean sameResultAsLast(Session s, Value[] params, Value[] lastParams, long lastEval) {
        if (!cacheableChecked) {
            long max = getMaxDataModificationId();
            cacheable = max != Long.MAX_VALUE;
            cacheableChecked = true;
        }
        if (!cacheable) {
            return false;
        }
        Database db = s.getDatabase();
        for (int i = 0; i < params.length; i++) {
            if (!db.areEqual(lastParams[i], params[i])) {
                return false;
            }
        }
        if (!isEverything(ExpressionVisitor.DETERMINISTIC_VISITOR) || !isEverything(ExpressionVisitor.INDEPENDENT_VISITOR)) {
            return false;
        }
        if (db.getModificationDataId() > lastEval && getMaxDataModificationId() > lastEval) {
            return false;
        }
        return true;
    }

    public final Value[] getParameterValues() {
        ArrayList<Parameter> list = getParameters();
        if (list == null) {
            list = New.arrayList();
        }
        int size = list.size();
        Value[] params = new Value[size];
        for (int i = 0; i < size; i++) {
            Value v = list.get(i).getParamValue();
            params[i] = v;
        }
        return params;
    }

    public LocalResult query(int maxrows) {
        return query(maxrows, null);
    }

    /**
     * Execute the query, writing the result to the target result.
     *
     * @param maxrows the maximum number of rows to return
     * @param target the target result (null will return the result)
     * @return the result set (if the target is not set).
     */
    LocalResult query(int limit, ResultTarget target) {
        fireBeforeSelectTriggers();
        if (!session.getDatabase().getOptimizeReuseResults()) {
            return queryWithoutCache(limit, target);
        }
        Value[] params = getParameterValues();
        long now = session.getDatabase().getModificationDataId();
        if (isEverything(ExpressionVisitor.DETERMINISTIC_VISITOR)) {
            if (lastResult != null && !lastResult.isClosed() && limit == lastLimit) {
                if (sameResultAsLast(session, params, lastParameters, lastEvaluated)) {
                    lastResult = lastResult.createShallowCopy(session);
                    if (lastResult != null) {
                        lastResult.reset();
                        return lastResult;
                    }
                }
            }
        }
        lastParameters = params;
        closeLastResult();
        lastResult = queryWithoutCache(limit, target);
        this.lastEvaluated = now;
        lastLimit = limit;
        return lastResult;
    }

    private void closeLastResult() {
        if (lastResult != null) {
            lastResult.close();
        }
    }

    /**
     * Initialize the order by list. This call may extend the expressions list.
     *
     * @param expressions the select list expressions
     * @param expressionSQL the select list SQL snippets
     * @param orderList the order by list
     * @param visible the number of visible columns in the select list
     * @param mustBeInResult all order by expressions must be in the select list
     * @return the new list (expressions may be added)
     */
    static void initOrder(ArrayList<Expression> expressions, ArrayList<String> expressionSQL, ArrayList<SelectOrderBy> orderList, int visible,
            boolean mustBeInResult) {
        for (SelectOrderBy o : orderList) {
            Expression e = o.expression;
            if (e == null) {
                continue;
            }
            // special case: SELECT 1 AS A FROM DUAL ORDER BY A
            // (oracle supports it, but only in order by, not in group by and
            // not in having):
            // SELECT 1 AS A FROM DUAL ORDER BY -A
            boolean isAlias = false;
            int idx = expressions.size();
            if (e instanceof ExpressionColumn) {
                ExpressionColumn exprCol = (ExpressionColumn) e;
                String tableAlias = exprCol.getOriginalTableAliasName();
                String col = exprCol.getOriginalColumnName();
                for (int j = 0; j < visible; j++) {
                    boolean found = false;
                    Expression ec = expressions.get(j);
                    if (ec instanceof ExpressionColumn) {
                        ExpressionColumn c = (ExpressionColumn) ec;
                        found = col.equals(c.getColumnName());
                        if (tableAlias != null && found) {
                            String ca = c.getOriginalTableAliasName();
                            if (ca != null) {
                                found = tableAlias.equals(ca);
                            }
                        }
                    } else if (!(ec instanceof Alias)) {
                        continue;
                    } else if (tableAlias == null && col.equals(ec.getAlias())) {
                        found = true;
                    } else {
                        Expression ec2 = ec.getNonAliasExpression();
                        if (ec2 instanceof ExpressionColumn) {
                            ExpressionColumn c2 = (ExpressionColumn) ec2;
                            String ta = exprCol.getSQL();
                            // exprCol.getTableAlias();
                            String tb = c2.getSQL();
                            // getTableAlias();
                            found = col.equals(c2.getColumnName());
                            if (!StringUtils.equals(ta, tb)) {
                                found = false;
                            }
                        }
                    }
                    if (found) {
                        idx = j;
                        isAlias = true;
                        break;
                    }
                }
            } else {
                String s = e.getSQL();
                if (expressionSQL != null) {
                    for (int j = 0, size = expressionSQL.size(); j < size; j++) {
                        String s2 = expressionSQL.get(j);
                        if (s2.equals(s)) {
                            idx = j;
                            isAlias = true;
                            break;
                        }
                    }
                }
            }
            if (!isAlias) {
                if (mustBeInResult) {
                    throw DbException.get(ErrorCode.ORDER_BY_NOT_IN_RESULT, e.getSQL());
                }
                expressions.add(e);
                String sql = e.getSQL();
                expressionSQL.add(sql);
            }
            o.columnIndexExpr = ValueExpression.get(ValueInt.get(idx + 1));
        }
    }

    /**
     * Create a {@link SortOrder} object given the list of {@link SelectOrderBy}
     * objects. The expression list is extended if necessary.
     *
     * @param orderList a list of {@link SelectOrderBy} elements
     * @param expressionCount the number of columns in the query
     * @return the {@link SortOrder} object
     */
    public SortOrder prepareOrder(ArrayList<SelectOrderBy> orderList, int expressionCount) {
        int size = orderList.size();
        int[] index = new int[size];
        int[] sortType = new int[size];
        for (int i = 0; i < size; i++) {
            SelectOrderBy o = orderList.get(i);
            int idx;
            boolean reverse = false;
            Expression expr = o.columnIndexExpr;
            Value v = expr.getValue(null);
            if (v == ValueNull.INSTANCE) {
                // parameter not yet set - order by first column
                idx = 0;
            } else {
                idx = v.getInt();
                if (idx < 0) {
                    reverse = true;
                    idx = -idx;
                }
                idx -= 1;
                if (idx < 0 || idx >= expressionCount) {
                    throw DbException.get(ErrorCode.ORDER_BY_NOT_IN_RESULT, "" + (idx + 1));
                }
            }
            index[i] = idx;
            boolean desc = o.descending;
            if (reverse) {
                desc = !desc;
            }
            int type = desc ? SortOrder.DESCENDING : SortOrder.ASCENDING;
            if (o.nullsFirst) {
                type += SortOrder.NULLS_FIRST;
            } else if (o.nullsLast) {
                type += SortOrder.NULLS_LAST;
            }
            sortType[i] = type;
        }
        return new SortOrder(session.getDatabase(), index, sortType);
    }

    public void setOffset(Expression offset) {
        this.offsetExpr = offset;
    }

    public void setLimit(Expression limit) {
        this.limitExpr = limit;
    }

    /**
     * Add a parameter to the parameter list.
     *
     * @param param the parameter to add
     */
    void addParameter(Parameter param) {
        if (parameters == null) {
            parameters = New.arrayList();
        }
        parameters.add(param);
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public final long getMaxDataModificationId() {
        ExpressionVisitor visitor = ExpressionVisitor.getMaxModificationIdVisitor();
        isEverything(visitor);
        return visitor.getMaxDataModificationId();
    }

}
