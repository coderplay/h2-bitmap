/*
 * Copyright 2004-2006 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.cases;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestHibernateClob {

    public static void main(String[] a) throws Exception {
        System.out.println("starting test...");
        org.h2.tools.DeleteDbFiles.execute(null, "test", true);
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:", "sa", "sa");
        conn.createStatement().execute("CREATE TABLE IF NOT EXISTS TEST(ID INT)");
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, "TEST", null);
        while(rs.next()) {
            String cat = rs.getString("TABLE_CAT");
            String schema = rs.getString("TABLE_SCHEM");
            String table = rs.getString("TABLE_NAME");
            ResultSet rs2 = meta.getColumns(cat, schema, table, null);
            while(rs2.next()) {
                System.out.println(table + "." + rs2.getString("COLUMN_NAME"));
            }
        }
        
        conn.getAutoCommit();
        conn.setAutoCommit(false);
        DatabaseMetaData dbMeta0 = 
        conn.getMetaData();
        dbMeta0.getDatabaseProductName();
        dbMeta0.getDatabaseMajorVersion();
        dbMeta0.getDatabaseProductVersion();
        dbMeta0.getDriverName();
        dbMeta0.getDriverVersion();
        dbMeta0.supportsResultSetType(1004);
        dbMeta0.supportsBatchUpdates();
        dbMeta0.dataDefinitionCausesTransactionCommit();
        dbMeta0.dataDefinitionIgnoredInTransactions();
        dbMeta0.supportsGetGeneratedKeys();
        conn.getAutoCommit();
        conn.getAutoCommit();
        conn.commit();
        conn.setAutoCommit(true);
        Statement stat0 = 
        conn.createStatement();
        stat0.executeUpdate("drop table CLOB_ENTITY if exists");
        stat0.getWarnings();
        stat0.executeUpdate("create table CLOB_ENTITY (ID bigint not null, SER_DATA clob, CLOB_DATA clob, primary key (ID))");
        stat0.getWarnings();
        stat0.close();
        conn.getWarnings();
        conn.clearWarnings();
        conn.setAutoCommit(false);
        conn.getAutoCommit();
        conn.getAutoCommit();
        PreparedStatement prep0 = 
        conn.prepareStatement("select max(ID) from CLOB_ENTITY");
        ResultSet rs0 = 
        prep0.executeQuery();
        rs0.next();
        rs0.getLong(1);
        rs0.wasNull();
        rs0.close();
        prep0.close();
        conn.getAutoCommit();
        PreparedStatement prep1 = 
        conn.prepareStatement("insert into CLOB_ENTITY (SER_DATA, CLOB_DATA, ID) values (?, ?, ?)");
        prep1.setNull(1, 2005);
        StringBuffer buff = new StringBuffer(20000);
        for(int i=0; i<10000; i++) {
            buff.append((char)('0' + (i%10)));
        }
        Reader x = new StringReader(buff.toString());
        prep1.setCharacterStream(2, x, 10000);
        prep1.setLong(3, 1);
        prep1.addBatch();
        prep1.executeBatch();
        prep1.close();
        conn.getAutoCommit();
        conn.getAutoCommit();
        conn.commit();
        conn.isClosed();
        conn.getWarnings();
        conn.clearWarnings();
        conn.getAutoCommit();
        conn.getAutoCommit();
        PreparedStatement prep2 = 
        conn.prepareStatement("select clobholdin0_.ID as ID0_0_, clobholdin0_.SER_DATA as SER2_0_0_, clobholdin0_.CLOB_DATA as CLOB3_0_0_ from CLOB_ENTITY clobholdin0_ where clobholdin0_.ID=?");
        prep2.setLong(1, 1);
        ResultSet rs1 = 
        prep2.executeQuery();
        rs1.next();
        System.out.println("ser: " + rs1.getCharacterStream("SER2_0_0_"));
        Clob clob0 = 
        rs1.getClob("CLOB3_0_0_");
        System.out.println("wasNull: " + rs1.wasNull());
        rs1.next();
        rs1.close();
        prep2.getMaxRows();
        prep2.getQueryTimeout();
        prep2.close();
        conn.getAutoCommit();
        Reader r = clob0.getCharacterStream();
        char[] chars = new char[(int)clob0.length()];
        int read = r.read(chars);
        System.out.println("read: " + read + " " + r);
        for(int i=0; i<10000; i++) {
            int ch = chars[i];
            if(ch != ('0' + (i%10))) {
                throw new Error("expected "+ (char)('0' + (i%10)) + " got: " + ch + " (" + (char)ch + ")");
            }
        }
        int ch = r.read();
        if(ch != -1) {
            System.out.println("expected -1 got: " + ch );
        }
        conn.close();
        System.out.println("done");
    }

}
