/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.alibaba.garuda.plan.logical.relational;

import com.alibaba.garuda.plan.FrontendException;
import com.alibaba.garuda.plan.Operator;
import com.alibaba.garuda.plan.PlanVisitor;
import com.alibaba.garuda.plan.logical.LogicalPlan;
import com.alibaba.garuda.plan.logical.expression.LogicalExpressionPlan;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class LOLimit extends LogicalRelationalOperator {
    private static final long NULL_LIMIT = -1;

    private long mLimit = NULL_LIMIT;
    private LogicalExpressionPlan mlimitPlan;

    public LOLimit(LogicalPlan plan) {
        super("LOLimit", plan);
    }

    public LOLimit(LogicalPlan plan, long limit) {
        super("LOLimit", plan);
        this.setLimit(limit);
    }

    public LOLimit(LogicalPlan plan, LogicalExpressionPlan limitPlan) {
        super("LOLimit", plan);
        this.setLimitPlan(limitPlan);
    }

    @Override
    public LogicalSchema getSchema() throws FrontendException {
        if (schema != null)
            return schema;

        LogicalRelationalOperator input = null;
        input = (LogicalRelationalOperator) plan.getPredecessors(this).get(0);

        schema = input.getSchema();
        return schema;
    }

    @Override
    public void accept(PlanVisitor v) throws FrontendException {

    }

    @Override
    public boolean isEqual(Operator other) throws FrontendException {
        if (other != null && other instanceof LOLimit) {
            LOLimit otherLimit = (LOLimit) other;
            if (this.getLimit() != NULL_LIMIT
                    && this.getLimit() == otherLimit.getLimit()
                    || this.getLimitPlan() != null
                    && this.getLimitPlan().isEqual(otherLimit.getLimitPlan()))
                return checkEquality(otherLimit);
        }
        return false;
    }

    public long getLimit() {
        return mLimit;
    }

    public void setLimit(long limit) {
        this.mLimit = limit;
    }

    public LogicalExpressionPlan getLimitPlan() {
        return mlimitPlan;
    }

    public void setLimitPlan(LogicalExpressionPlan mlimitPlan) {
        this.mlimitPlan = mlimitPlan;
    }

    public Operator getInput(LogicalPlan plan) {
        return plan.getPredecessors(this).get(0);
    }
}
