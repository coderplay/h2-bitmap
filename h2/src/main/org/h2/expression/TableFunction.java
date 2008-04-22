/*
 * Copyright 2004-2008 H2 Group. Multiple-Licensed under the H2 License, 
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.expression;

import java.sql.SQLException;

import org.h2.constant.ErrorCode;
import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.message.Message;
import org.h2.result.LocalResult;
import org.h2.table.Column;
import org.h2.tools.SimpleResultSet;
import org.h2.util.MathUtils;
import org.h2.util.ObjectArray;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2.value.ValueArray;
import org.h2.value.ValueNull;
import org.h2.value.ValueResultSet;

/**
 * Implementation of the functions TABLE(..) and TABLE_DISTINCT(..).
 */
public class TableFunction extends Function implements FunctionCall {
    private boolean distinct;
    private Column[] columnList;

    TableFunction(Database database, FunctionInfo info) {
        super(database, info);
        distinct = info.type == Function.TABLE_DISTINCT;
    }

    public Value getValue(Session session) throws SQLException {
        return getTable(session, args, false, distinct);
    }

    protected void checkParameterCount(int len) throws SQLException {
        if (len < 1) {
            throw Message.getSQLException(ErrorCode.INVALID_PARAMETER_COUNT_2, new String[] { getName(),
                    ">0" });
        }
    }

    public String getSQL() {
        StringBuffer buff = new StringBuffer();
        buff.append(getName());
        buff.append('(');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                buff.append(", ");
            }
            buff.append(columnList[i].getCreateSQL());
            buff.append("=");
            Expression e = args[i];
            buff.append(e.getSQL());
        }
        buff.append(')');
        return buff.toString();
    }


    public String getName() {
        return distinct ? "TABLE_DISTINCT" : "TABLE";
    }

    public int getRowCount(Session session) throws SQLException {
        int len = columnList.length;
        int rowCount = 0;
        for (int i = 0; i < len; i++) {
            Expression expr = args[i];
            if (expr.isConstant()) {
                Value v = expr.getValue(session);
                if (v != ValueNull.INSTANCE) {
                    ValueArray array = (ValueArray) v.convertTo(Value.ARRAY);
                    Value[] l = array.getList();
                    rowCount = Math.max(rowCount, l.length);
                }
            }
        }
        return rowCount;
    }

    public ValueResultSet getValueForColumnList(Session session, Expression[] nullArgs) throws SQLException {
        return getTable(session, args, true, false);
    }

    public void setColumns(ObjectArray columns) {
        this.columnList = new Column[columns.size()];
        columns.toArray(columnList);
    }

    private ValueResultSet getTable(Session session, Expression[] args, boolean onlyColumnList, boolean distinct) throws SQLException {
        int len = columnList.length;
        Expression[] header = new Expression[len];
        Database db = session.getDatabase();
        for (int i = 0; i < len; i++) {
            Column c = columnList[i];
            ExpressionColumn col = new ExpressionColumn(db, c);
            header[i] = col;
        }
        LocalResult result = new LocalResult(session, header, len);
        if (distinct) {
            result.setDistinct();
        }
        if (!onlyColumnList) {
            Value[][] list = new Value[len][];
            int rowCount = 0;
            for (int i = 0; i < len; i++) {
                Value v = args[i].getValue(session);
                if (v == ValueNull.INSTANCE) {
                    list[i] = new Value[0];
                } else {
                    ValueArray array = (ValueArray) v.convertTo(Value.ARRAY);
                    Value[] l = array.getList();
                    list[i] = l;
                    rowCount = Math.max(rowCount, l.length);
                }
            }
            for (int row = 0; row < rowCount; row++) {
                Value[] r = new Value[len];
                for (int j = 0; j < len; j++) {
                    Value[] l = list[j];
                    Value v;
                    if (l.length <= row) {
                        v = ValueNull.INSTANCE;
                    } else {
                        Column c = columnList[j];
                        v = l[row];
                        v = v.convertTo(c.getType());
                        v = v.convertPrecision(c.getPrecision());
                        v = v.convertScale(true, c.getScale());
                    }
                    r[j] = v;
                }
                result.addRow(r);
            }
        }
        result.done();
        ValueResultSet vr = ValueResultSet.get(getSimpleResultSet(result, Integer.MAX_VALUE));
        return vr;
    }

    SimpleResultSet getSimpleResultSet(LocalResult rs,  int maxrows) throws SQLException {
        int columnCount = rs.getVisibleColumnCount();
        SimpleResultSet simple = new SimpleResultSet();
        for (int i = 0; i < columnCount; i++) {
            String name = rs.getColumnName(i);
            int sqlType = DataType.convertTypeToSQLType(rs.getColumnType(i));
            int precision = MathUtils.convertLongToInt(rs.getColumnPrecision(i));
            int scale = rs.getColumnScale(i);
            simple.addColumn(name, sqlType, precision, scale);
        }
        rs.reset();
        for (int i = 0; i < maxrows && rs.next(); i++) {
            Object[] list = new Object[columnCount];
            for (int j = 0; j < columnCount; j++) {
                list[j] = rs.currentRow()[j].getObject();
            }
            simple.addRow(list);
        }
        return simple;
    }

}
