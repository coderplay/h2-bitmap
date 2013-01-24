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
public class LOFilter extends LogicalRelationalOperator {

    private LogicalExpressionPlan filterPlan;

    public LOFilter(LogicalPlan plan) {
        super("LOFilter", plan);
    }

    public LOFilter(LogicalPlan plan, LogicalExpressionPlan filterPlan) {
        super("LOFilter", plan);
        this.filterPlan = filterPlan;
    }

    /**
     * return condition
     * 
     * @return
     */
    public LogicalExpressionPlan getFilterPlan() {
        return filterPlan;
    }

    public void setFilterPlan(LogicalExpressionPlan filterPlan) {
        this.filterPlan = filterPlan;
    }

    @Override
    public LogicalSchema getSchema() throws FrontendException {
        return null;
    }

    @Override
    public void accept(PlanVisitor v) throws FrontendException {

    }

    @Override
    public boolean isEqual(Operator other) throws FrontendException {
        if (other != null && other instanceof LOFilter) {
            LOFilter of = (LOFilter) other;
            return filterPlan.isEqual(of.filterPlan) && checkEquality(of);
        } else {
            return false;
        }
    }

}
