/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.alibaba.garuda.parser.ast.statement;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.garuda.parser.visitor.GarudaASTVisitor;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
@SuppressWarnings("serial")
public class GarudaSelectQueryBlock extends SQLSelectQueryBlock {

    private SQLOrderBy           orderBy;

    private Limit                limit;
    
    public SQLOrderBy getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(SQLOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor instanceof GarudaASTVisitor) {
            accept0((GarudaASTVisitor) visitor);
            return;
        }

        if (visitor.visit(this)) {
            acceptChild(visitor, this.selectList);
            acceptChild(visitor, this.from);
            acceptChild(visitor, this.where);
            acceptChild(visitor, this.groupBy);
            acceptChild(visitor, this.orderBy);
            acceptChild(visitor, this.limit);
            acceptChild(visitor, this.into);
        }

        visitor.endVisit(this);
    }

    public void accept0(GarudaASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.selectList);
            acceptChild(visitor, this.from);
            acceptChild(visitor, this.where);
            acceptChild(visitor, this.groupBy);
            acceptChild(visitor, this.orderBy);
            acceptChild(visitor, this.limit);
            acceptChild(visitor, this.into);
        }

        visitor.endVisit(this);
    }

    public static class Limit extends SQLObjectImpl {

        public Limit(){

        }

        private SQLExpr rowCount;
        private SQLExpr offset;

        public SQLExpr getRowCount() {
            return rowCount;
        }

        public void setRowCount(SQLExpr rowCount) {
            this.rowCount = rowCount;
        }

        public SQLExpr getOffset() {
            return offset;
        }

        public void setOffset(SQLExpr offset) {
            this.offset = offset;
        }

        @Override
        protected void accept0(SQLASTVisitor visitor) {
            if (visitor instanceof GarudaASTVisitor) {
                GarudaASTVisitor garudaVisitor = (GarudaASTVisitor) visitor;

                if (garudaVisitor.visit(this)) {
                    acceptChild(visitor, offset);
                    acceptChild(visitor, rowCount);
                }
            }
        }

    }
}
