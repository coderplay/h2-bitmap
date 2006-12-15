/*
 * Copyright 2004-2006 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jdbcx;

import java.sql.SQLException;

import org.h2.jdbc.JdbcConnection;

public interface JdbcConnectionListener {
    
    // TODO pooled connection: make sure fatalErrorOccured is called in the right situations
    void fatalErrorOccured(JdbcConnection conn, SQLException e) throws SQLException;
    
    void closed(JdbcConnection conn);
}
