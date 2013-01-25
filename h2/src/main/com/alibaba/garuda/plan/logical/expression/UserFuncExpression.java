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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.garuda.data.DataType;
import com.alibaba.garuda.plan.FrontendException;
import com.alibaba.garuda.plan.Operator;
import com.alibaba.garuda.plan.OperatorPlan;
import com.alibaba.garuda.plan.PlanVisitor;
import com.alibaba.garuda.plan.logical.relational.LogicalSchema;

public class UserFuncExpression extends LogicalExpression {

    private String signature;
    private static int sigSeq=0;
    private boolean viaDefine=false; //this represents whether the function was instantiate via a DEFINE statement or not

    public UserFuncExpression(OperatorPlan plan) {
        super("UserFunc", plan);
        plan.add(this);
        if (signature == null) {
            signature = Integer.toString(sigSeq++);
        }
    }

    @Override
    public void accept(PlanVisitor v) throws FrontendException {

    }

    @Override
    public boolean isEqual(Operator other) throws FrontendException {

        //For the purpose of optimization rules (specially LogicalExpressionSimplifier)
        // a non deterministic udf is not equal to another. So returning false for
        //such cases.
        // Note that the function is also invoked by implementations of OperatorPlan.isEqual
        // that function is called from test cases to compare logical plans, and
        // it will return false even if the plans are clones.
        if(!this.isDeterministic())
            return false;

        if( other instanceof UserFuncExpression ) {
            UserFuncExpression exp = (UserFuncExpression)other;


            List<Operator> mySuccs = getPlan().getSuccessors(this);
            List<Operator> theirSuccs = other.getPlan().getSuccessors(other);
            if(mySuccs == null || theirSuccs == null){
                if(mySuccs == null && theirSuccs == null){
                    return true;
                }else{
                    //only one of the udfs has null successors
                    return false;
                }
            }
            if (mySuccs.size()!=theirSuccs.size())
                return false;
            for (int i=0;i<mySuccs.size();i++) {
                if (!mySuccs.get(i).isEqual(theirSuccs.get(i)))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isDeterministic() throws FrontendException{

        return false;

    }


    public List<LogicalExpression> getArguments() throws FrontendException {
        List<Operator> successors = null;
        List<LogicalExpression> args = new ArrayList<LogicalExpression>();
//        try {
            successors = plan.getSuccessors(this);

            if(successors == null)
                return args;

            for(Operator lo : successors){
                args.add((LogicalExpression)lo);
            }
//        } catch (FrontendException e) {
//           return args;
//        }
        return args;
    }


    @Override
    public LogicalSchema.LogicalFieldSchema getFieldSchema() throws FrontendException {
       return null;
    }

    @Override
    public LogicalExpression deepCopy(LogicalExpressionPlan lgExpPlan) throws FrontendException {
        UserFuncExpression copy =  null;
//        try {
            copy = new UserFuncExpression(
                    lgExpPlan);

            copy.signature = signature;
            // Deep copy the input expressions.
            List<Operator> inputs = plan.getSuccessors( this );
            if( inputs != null ) {
                for( Operator op : inputs ) {
                    LogicalExpression input = (LogicalExpression)op;
                    LogicalExpression inputCopy = input.deepCopy( lgExpPlan );
                    lgExpPlan.add( inputCopy );
                    lgExpPlan.connect( copy, inputCopy );
                }
            }

//        } catch(CloneNotSupportedException e) {
//             e.printStackTrace();
//        }

        return copy;
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("(Name: " + name + "(" +  ")" + " Type: ");
        if (fieldSchema!=null)
            msg.append(DataType.findTypeName(fieldSchema.type));
        else
            msg.append("null");
        msg.append(" Uid: ");
        if (fieldSchema!=null)
            msg.append(fieldSchema.uid);
        else
            msg.append("null");
        msg.append(")");

        return msg.toString();
    }

    public String getSignature() {
        return signature;
    }

    public boolean isViaDefine() {
        return viaDefine;
    }
}
