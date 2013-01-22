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
package com.alibaba.garuda.parser.visitor;

import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.alibaba.garuda.parser.ast.statement.GarudaSelectQueryBlock;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class GarudaASTVisitorAdapter extends SQLASTVisitorAdapter implements
        GarudaASTVisitor {

    @Override
    public boolean visit(GarudaSelectQueryBlock x) {
        return true;
    }

    @Override
    public void endVisit(GarudaSelectQueryBlock x) {

    }

    @Override
    public boolean visit(GarudaSelectQueryBlock.Limit x) {
        return true;
    }

    @Override
    public void endVisit(GarudaSelectQueryBlock.Limit x) {

    }
}
