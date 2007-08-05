/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.ddl;

import java.sql.SQLException;

import org.h2.engine.Database;
import org.h2.engine.Right;
import org.h2.engine.Session;
import org.h2.message.Message;
import org.h2.schema.Schema;
import org.h2.table.Table;

/**
 * @author Thomas
 */
public class DropTable extends SchemaCommand {

    private boolean ifExists;
    private String tableName;
    private Table table;
    private DropTable next;

    public DropTable(Session session, Schema schema) {
        super(session, schema);
    }

    public void addNextDropTable(DropTable next) {
        if(this.next == null) {
            this.next = next;
        } else {
            this.next.addNextDropTable(next);
        }
    }

    public void setIfExists(boolean b) {
        ifExists = b;
        if(next != null) {
            next.setIfExists(b);
        }
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private void prepareDrop() throws SQLException {
        table = getSchema().findTableOrView(session, tableName);
        // TODO drop table: drops views as well (is this ok?)
        if(table == null) {
            if(!ifExists) {
                throw Message.getSQLException(Message.TABLE_OR_VIEW_NOT_FOUND_1, tableName);
            }
        } else {
            session.getUser().checkRight(table, Right.ALL);
            if(!table.canDrop()) {
                throw Message.getSQLException(Message.CANNOT_DROP_TABLE_1, tableName);
            }
            table.lock(session, true);
        }
        if(next != null) {
            next.prepareDrop();
        }
    }

    private void executeDrop() throws SQLException {
        // need to get the table again, because it may be dropped already meanwhile (dependent object, or same object)
        table = getSchema().findTableOrView(session, tableName);
        if(table != null) {
            table.setModified();
            Database db = session.getDatabase();
            db.removeSchemaObject(session, table);
        }
        if(next != null) {
            next.executeDrop();
        }
    }

    public int update() throws SQLException {
        session.commit(true);
        prepareDrop();
        executeDrop();
        return 0;
    }

}
