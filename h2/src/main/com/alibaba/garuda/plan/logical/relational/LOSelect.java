/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.alibaba.garuda.plan.logical.relational;

import java.util.List;

import com.alibaba.garuda.plan.FrontendException;
import com.alibaba.garuda.plan.Operator;
import com.alibaba.garuda.plan.PlanVisitor;
import com.alibaba.garuda.plan.logical.LogicalPlan;
import com.alibaba.garuda.plan.logical.expression.LogicalExpressionPlan;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class LOSelect extends LogicalRelationalOperator {

    private List<LogicalExpressionPlan> mselectPlans;

    public LOSelect(LogicalPlan plan, List<LogicalExpressionPlan> selectPlans) {
        super("LOSelect", plan);
        setSelectPlans(selectPlans);
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
        if (other != null && other instanceof LOSelect) {
            LOSelect of = (LOSelect) other;
            return checkEquality(of);
        } else {
            return false;
        }
    }

    public List<LogicalExpressionPlan> getSelectPlans() {
        return mselectPlans;
    }

    public void setSelectPlans(List<LogicalExpressionPlan> selectPlans) {
        mselectPlans = selectPlans;
    }

}
