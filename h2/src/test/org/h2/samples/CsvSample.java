/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0
 * (license2)
 * Initial Developer: H2 Group
 */
package org.h2.samples;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.h2.tools.Csv;
import org.h2.tools.SimpleResultSet;

/**
 * This sample application shows how to use the CSV tool
 * to write CSV (comma separated values) files, and
 * how to use the tool to read such files.
 * See also the section CSV (Comma Separated Values) Support in the Tutorial.
 */
public class CsvSample {

    public static void main(String[] args) throws SQLException {
        CsvSample.write();
        CsvSample.read();
    }

    /**
     * Write a CSV file.
     */
    static void write() throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("NAME", Types.VARCHAR, 255, 0);
        rs.addColumn("EMAIL", Types.VARCHAR, 255, 0);
        rs.addColumn("PHONE", Types.VARCHAR, 255, 0);
        rs.addRow(new String[] { "Bob Meier", "bob.meier@abcde.abc", "+41123456789" });
        rs.addRow(new String[] { "John Jones", "john.jones@abcde.abc", "+41976543210" });
        Csv.getInstance().write("data/test.csv", rs, null);
    }

    /**
     * Read a CSV file.
     */
    static void read() throws SQLException {
        ResultSet rs = Csv.getInstance().read("data/test.csv", null, null);
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            for (int i = 0; i < meta.getColumnCount(); i++) {
                System.out.println(meta.getColumnLabel(i + 1) + ": " + rs.getString(i + 1));
            }
            System.out.println();
        }
        rs.close();
    }
}
