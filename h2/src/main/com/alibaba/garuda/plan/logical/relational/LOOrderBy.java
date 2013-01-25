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
public class LOOrderBy extends LogicalRelationalOperator {

    private List<Boolean> mAscCols;
    private List<LogicalExpressionPlan> mSortColPlans;

    public LOOrderBy(LogicalPlan plan) {
        super("LOOrderBy", plan);
    }

    public LOOrderBy(LogicalPlan plan,
            List<LogicalExpressionPlan> sortColPlans, List<Boolean> ascCols) {
        this(plan);
        mSortColPlans = sortColPlans;
        mAscCols = ascCols;
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
        if (other != null && other instanceof LOOrderBy) {
            LOOrderBy of = (LOOrderBy) other;
            return checkEquality(of);
        } else {
            return false;
        }
    }

    public List<Boolean> getAscendingCols() {
        return mAscCols;
    }

    public void setAscendingCols(List<Boolean> ascCols) {
        mAscCols = ascCols;
    }

    public List<LogicalExpressionPlan> getSortColPlans() {
        return mSortColPlans;
    }

    public void setSortColPlans(List<LogicalExpressionPlan> sortPlans) {
        mSortColPlans = sortPlans;
    }
}
