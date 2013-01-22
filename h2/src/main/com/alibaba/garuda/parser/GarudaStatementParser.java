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
package com.alibaba.garuda.parser;

import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.SQLStatementParser;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class GarudaStatementParser extends SQLStatementParser {

    public GarudaStatementParser(String sql) {
        super(new GarudaExprParser(sql));
    }

    public GarudaStatementParser(Lexer lexer){
        super(new GarudaExprParser(lexer));
    }

    public SQLSelectStatement parseSelect() {
        return new SQLSelectStatement(
                new GarudaSelectParser(this.exprParser).select());
    }
}
