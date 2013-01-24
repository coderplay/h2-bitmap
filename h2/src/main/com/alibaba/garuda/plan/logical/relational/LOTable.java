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

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class LOTable extends LogicalRelationalOperator {

    public LOTable(LogicalPlan plan) {
        // TODO:
        super("LOTable", plan);
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
        if (other != null && other instanceof LOTable) {
            LOTable o = (LOTable) other;
            return checkEquality(o);
        } else {
            return false;
        }
    }

}
