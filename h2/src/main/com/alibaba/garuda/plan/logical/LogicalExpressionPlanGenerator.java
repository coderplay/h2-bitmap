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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.garuda.parser.visitor.GarudaASTVisitorAdapter;
import com.alibaba.garuda.plan.logical.expression.AddExpression;
import com.alibaba.garuda.plan.logical.expression.AndExpression;
import com.alibaba.garuda.plan.logical.expression.ConstantExpression;
import com.alibaba.garuda.plan.logical.expression.DivideExpression;
import com.alibaba.garuda.plan.logical.expression.EqualExpression;
import com.alibaba.garuda.plan.logical.expression.GreaterThanEqualExpression;
import com.alibaba.garuda.plan.logical.expression.GreaterThanExpression;
import com.alibaba.garuda.plan.logical.expression.LessThanEqualExpression;
import com.alibaba.garuda.plan.logical.expression.LessThanExpression;
import com.alibaba.garuda.plan.logical.expression.LogicalExpression;
import com.alibaba.garuda.plan.logical.expression.LogicalExpressionPlan;
import com.alibaba.garuda.plan.logical.expression.MultiplyExpression;
import com.alibaba.garuda.plan.logical.expression.OrExpression;
import com.alibaba.garuda.plan.logical.expression.ProjectExpression;
import com.alibaba.garuda.plan.logical.expression.RegexExpression;
import com.alibaba.garuda.plan.logical.expression.SubtractExpression;
import com.alibaba.garuda.plan.logical.expression.UserFuncExpression;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class LogicalExpressionPlanGenerator extends GarudaASTVisitorAdapter {
    
    private static final String EXPRESSION_CACHE = "expression.cache";

    private LogicalExpressionPlan exprPlan = new LogicalExpressionPlan();

//    private LogicalRelationalOperator parent;

//    public LogicalExpressionPlanGenerator(LogicalRelationalOperator parent)  {
//        this.parent = parent;
//    }
    
    
    public LogicalExpressionPlan getPlan() {
        return exprPlan;
    }

    @Override
    public boolean visit(SQLIdentifierExpr x) {
//        x.putAttribute(EXPRESSION_CACHE, new ProjectExpression(exprPlan, 0, 0,
//                parent));
        x.putAttribute(EXPRESSION_CACHE, new ProjectExpression(exprPlan, 0, 0,
                null));
        return false;
    }

    @Override
    public boolean visit(SQLPropertyExpr x) {
//        x.getOwner().accept(this);
//        print(".");
//        print(x.getName());
        return false;
    }

    @Override
    public boolean visit(SQLCharExpr x) {
        x.putAttribute(EXPRESSION_CACHE,
                new ConstantExpression(exprPlan, x.getText()));
        return false;
    }

    @Override
    public boolean visit(SQLVariantRefExpr x) {
        return false;
    }

    @Override
    public boolean visit(SQLUnaryExpr x) {
        return false;
    }

    public boolean visit(SQLBinaryOpExpr x) {
        SQLExpr left = x.getLeft();
        SQLExpr right = x.getRight();
        left.accept(this);
        if (!left.getAttributes().containsKey(EXPRESSION_CACHE)) {
            return false;
        }

        right.accept(this);
        if (!right.getAttributes().containsKey(EXPRESSION_CACHE)) {
            return false;
        }

        LogicalExpression value = null;
        switch (x.getOperator()) {
        case BooleanAnd:
            value = new AndExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttributes().get(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case BooleanOr:
            value = new OrExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttributes().get(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case Add:
            value = new AddExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttributes().get(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case Subtract:
            value = new SubtractExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case Multiply:
            value = new MultiplyExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case Divide:
            value = new DivideExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case GreaterThan:
            value = new GreaterThanExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case GreaterThanOrEqual:
            value = new GreaterThanEqualExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case LessThan:
            value = new LessThanExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case LessThanOrEqual:
            value = new LessThanEqualExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case Is:
        case Equality:
            value = new EqualExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
            break;
        case IsNot:
            // TODO:
            break;
        case RegExp:
        case RLike: {
            value = new RegexExpression(exprPlan,
                    (LogicalExpression) left.getAttribute(EXPRESSION_CACHE),
                    (LogicalExpression) right.getAttribute(EXPRESSION_CACHE));
            x.putAttribute(EXPRESSION_CACHE, value);
        }
            break;
        default:
            break;
        }

        return false;
    }

    @Override
    public boolean visit(SQLIntegerExpr x) {
        x.putAttribute(EXPRESSION_CACHE,
                new ConstantExpression(exprPlan, x.getNumber()));
        return false;
    }

    @Override
    public boolean visit(SQLNumberExpr x) {
        x.putAttribute(EXPRESSION_CACHE,
                new ConstantExpression(exprPlan, x.getNumber()));
        return false;
    }

    @Override
    public boolean visit(SQLCaseExpr x) {
        return false;
    }

    @Override
    public boolean visit(SQLInListExpr x) {
        return false;
    }

    @Override
    public boolean visit(SQLNullExpr x) {
        return false;
    }

    @Override
    public boolean visit(SQLMethodInvokeExpr x) {
        x.putAttribute(EXPRESSION_CACHE, new UserFuncExpression(exprPlan));
        return false;
    }
    
    @Override
    public boolean visit(SQLAggregateExpr x) {
        x.putAttribute(EXPRESSION_CACHE, new UserFuncExpression(exprPlan));
        return false;
    }

    @Override
    public boolean visit(SQLQueryExpr x) {
        return false;
    }

}
