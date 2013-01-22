/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.garuda.parser.ast.statement.GarudaSelectQueryBlock;
import com.alibaba.garuda.parser.ast.statement.GarudaSelectQueryBlock.Limit;

public class GarudaExprParser extends SQLExprParser {

    public GarudaExprParser(Lexer lexer){
        super(lexer);
    }

    public GarudaExprParser(String sql) {
        this(new GarudaLexer(sql));
        this.lexer.nextToken();
    }
    
    public Limit parseLimit() {
        if (lexer.token() == Token.LIMIT) {
            lexer.nextToken();

            GarudaSelectQueryBlock.Limit limit = new GarudaSelectQueryBlock.Limit();

            SQLExpr temp = this.expr();
            if (lexer.token() == (Token.COMMA)) {
                limit.setOffset(temp);
                lexer.nextToken();
                limit.setRowCount(this.expr());
            } else if (identifierEquals("OFFSET")) {
                limit.setRowCount(temp);
                lexer.nextToken();
                limit.setOffset(this.expr());
            } else {
                limit.setRowCount(temp);
            }
            return limit;
        }

        return null;
    }
    
    
    public SQLSelectParser createSelectParser() {
        return new GarudaSelectParser(this);
    }


}
