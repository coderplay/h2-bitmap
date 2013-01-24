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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.h2.test.TestBase;

import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.garuda.parser.GarudaStatementParser;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class TestLogicalPlanPrinter extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String[] args) throws Exception {
        TestBase.createCaller().init().test();
    }

    @Override
    public void test() throws Exception {
        testLogicalPlanPrinter();
    }

    private void testLogicalPlanPrinter() throws Exception {

        String sql = "SELECT NAME, COUNT(ID) from USER JOIN ITEM ON USER.ID = ITEM.USERID "
                + "JOIN ORDERS ON ORDERS.ID = USER.ID "
                + "WHERE GENDER=1 AND NAME='min zhou' OR (FOO RLIKE 'foo') "
                + "GROUP BY NAME ORDER BY NAME LIMIT 100;";

        SQLStatementParser parser = new GarudaStatementParser(sql);
        SQLSelectStatement stmt = parser.parseSelect();
        
        LogicalPlanGenerator gen = new LogicalPlanGenerator();
        stmt.accept(gen);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        LogicalPlanPrinter pp = new LogicalPlanPrinter(gen.getLogicalPlan(), ps);
        pp.visit();
        
//        DotLOPrinter dp = new DotLOPrinter(gen.getLogicalPlan(), ps);
//        dp.dump();
        
        System.out.println(out.toString());
    }

}
