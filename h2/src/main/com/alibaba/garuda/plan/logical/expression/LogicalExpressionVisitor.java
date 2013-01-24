/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.garuda.plan.logical.expression;

import com.alibaba.garuda.plan.FrontendException;
import com.alibaba.garuda.plan.OperatorPlan;
import com.alibaba.garuda.plan.PlanVisitor;
import com.alibaba.garuda.plan.PlanWalker;

/**
 * A visitor for expression plans.
 */
public abstract class LogicalExpressionVisitor extends PlanVisitor {

    protected LogicalExpressionVisitor(OperatorPlan p, PlanWalker walker)
            throws FrontendException {
        super(p, walker);

        if (!(plan instanceof LogicalExpressionPlan)) {
            throw new FrontendException(
                    "LogicalExpressionVisitor expects to visit "
                            + "expression plans.");
        }
    }

    public void visit(AndExpression op) throws FrontendException {
    }

    public void visit(OrExpression op) throws FrontendException {
    }

    public void visit(EqualExpression op) throws FrontendException {
    }

    public void visit(ProjectExpression op) throws FrontendException {
    }

    public void visit(ConstantExpression op) throws FrontendException {
    }

    public void visit(GreaterThanExpression op) throws FrontendException {
    }

    public void visit(GreaterThanEqualExpression op) throws FrontendException {
    }

    public void visit(LessThanExpression op) throws FrontendException {
    }

    public void visit(LessThanEqualExpression op) throws FrontendException {
    }

    public void visit(NotEqualExpression op) throws FrontendException {
    }

    public void visit(NotExpression op) throws FrontendException {
    }

    public void visit(IsNullExpression op) throws FrontendException {
    }

    public void visit(NegativeExpression op) throws FrontendException {
    }

    public void visit(AddExpression op) throws FrontendException {
    }

    public void visit(SubtractExpression op) throws FrontendException {
    }

    public void visit(MultiplyExpression op) throws FrontendException {
    }

    public void visit(ModExpression op) throws FrontendException {
    }

    public void visit(DivideExpression op) throws FrontendException {
    }

    public void visit(MapLookupExpression op) throws FrontendException {
    }

    public void visit(BinCondExpression op) throws FrontendException {
    }


    public void visit(RegexExpression op) throws FrontendException {
    }

}
