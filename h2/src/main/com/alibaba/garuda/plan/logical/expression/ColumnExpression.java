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

import com.alibaba.garuda.plan.OperatorPlan;

/**
 * Super class for all column expressions, including projection, constants, and deferences.
 *
 */
public abstract class ColumnExpression extends LogicalExpression {

    /**
     * 
     * @param name of the operator
     * @param plan LogicalExpressionPlan this column expression is part of
     */
    public ColumnExpression(String name, OperatorPlan plan) {
        super(name, plan);
    }

}
