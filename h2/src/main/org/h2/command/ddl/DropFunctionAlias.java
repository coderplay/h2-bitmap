/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.ddl;

import java.sql.SQLException;

import org.h2.engine.Database;
import org.h2.engine.FunctionAlias;
import org.h2.engine.Session;
import org.h2.message.Message;

public class DropFunctionAlias extends DefineCommand {

    private String aliasName;
    private boolean ifExists;
    
    public DropFunctionAlias(Session session) {
        super(session);
    }

    public int update() throws SQLException {
        session.getUser().checkAdmin();
        session.commit(true);
        Database db = session.getDatabase();
        FunctionAlias functionAlias = db.findFunctionAlias(aliasName);
        if(functionAlias == null) {
            if(!ifExists) {
                throw Message.getSQLException(Message.FUNCTION_ALIAS_NOT_FOUND_1, aliasName);
            }
        } else {
            db.removeDatabaseObject(session, functionAlias);
        }
        return 0;
    }
    
    public void setAliasName(String name) {
        this.aliasName = name;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }    

}
