/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.alibaba.garuda.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.garuda.parser.ast.statement.GarudaSelectQueryBlock;
import com.alibaba.garuda.parser.ast.statement.GarudaSelectQueryBlock.Limit;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class GarudaSelectParser extends SQLSelectParser {

    public GarudaSelectParser(SQLExprParser exprParser) {
        super(exprParser);
    }

    @Override
    public SQLSelectQuery query() {
        if (lexer.token() == (Token.LPAREN)) {
            lexer.nextToken();

            SQLSelectQuery select = query();
            accept(Token.RPAREN);

            return queryRest(select);
        }

        GarudaSelectQueryBlock queryBlock = new GarudaSelectQueryBlock();

        if (lexer.token() == Token.SELECT) {
            lexer.nextToken();
            
            if (lexer.token() == (Token.DISTINCT)) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
                lexer.nextToken();
            } else if (lexer.token() == (Token.ALL)) {
                queryBlock.setDistionOption(SQLSetQuantifier.ALL);
                lexer.nextToken();
            }

            parseSelectList(queryBlock);

            if (lexer.token() == (Token.INTO)) {
                lexer.nextToken();

                if (identifierEquals("OUTFILE")) {
                    lexer.nextToken();

                    MySqlOutFileExpr outFile = new MySqlOutFileExpr();
                    outFile.setFile(expr());

                    queryBlock.setInto(outFile);

                    if (identifierEquals("FIELDS")
                            || identifierEquals("COLUMNS")) {
                        lexer.nextToken();

                        if (identifierEquals("TERMINATED")) {
                            lexer.nextToken();
                            accept(Token.BY);
                        }
                        outFile.setColumnsTerminatedBy((SQLLiteralExpr) expr());

                        if (identifierEquals("OPTIONALLY")) {
                            lexer.nextToken();
                            outFile.setColumnsEnclosedOptionally(true);
                        }

                        if (identifierEquals("ENCLOSED")) {
                            lexer.nextToken();
                            accept(Token.BY);
                            outFile.setColumnsEnclosedBy((SQLLiteralExpr) expr());
                        }

                        if (identifierEquals("ESCAPED")) {
                            lexer.nextToken();
                            accept(Token.BY);
                            outFile.setColumnsEscaped((SQLLiteralExpr) expr());
                        }
                    }

                    if (identifierEquals("LINES")) {
                        lexer.nextToken();

                        if (identifierEquals("STARTING")) {
                            lexer.nextToken();
                            accept(Token.BY);
                            outFile.setLinesStartingBy((SQLLiteralExpr) expr());
                        } else {
                            identifierEquals("TERMINATED");
                            lexer.nextToken();
                            accept(Token.BY);
                            outFile.setLinesTerminatedBy((SQLLiteralExpr) expr());
                        }
                    }
                } else {
                    queryBlock.setInto(this.exprParser.name());
                }
            }
        }

        parseFrom(queryBlock);

        parseWhere(queryBlock);

        parseGroupBy(queryBlock);

        queryBlock.setOrderBy(this.exprParser.parseOrderBy());

        if (lexer.token() == Token.LIMIT) {
            queryBlock.setLimit(parseLimit());
        }

        if (lexer.token() == Token.INTO) {
            lexer.nextToken();
            SQLExpr expr = this.exprParser.name();
            queryBlock.setInto(expr);
        }


        return queryRest(queryBlock);
    }
    
    public Limit parseLimit() {
        return ((GarudaExprParser)this.exprParser) .parseLimit();
    }

}
