/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.value;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.constant.ErrorCode;
import org.h2.constant.SysProperties;
import org.h2.message.Message;
import org.h2.util.ObjectUtils;

public class ValueShort extends Value {
    public static final int PRECISION = 5;
    public static final int DISPLAY_SIZE = 6; // -32768    

    private final short value;

    private ValueShort(short value) {
        this.value = value;
    }

    public Value add(Value v) throws SQLException {
        ValueShort other = (ValueShort) v;
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(value + other.value);
        }
        return ValueShort.get((short) (value + other.value));
    }

    private ValueShort checkRange(int value) throws SQLException {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw Message.getSQLException(ErrorCode.OVERFLOW_FOR_TYPE_1, DataType.getDataType(Value.SHORT).name);
        } else {
            return ValueShort.get((short) value);
        }
    }       

    public int getSignum() {
        return value == 0 ? 0 : (value < 0 ? -1 : 1);
    }

    public Value negate() throws SQLException {
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(-(int) value);
        }
        return ValueShort.get((short) (-value));
    }

    public Value subtract(Value v) throws SQLException {
        ValueShort other = (ValueShort) v;
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(value - other.value);
        }
        return ValueShort.get((short) (value - other.value));
    }

    public Value multiply(Value v) throws SQLException {
        ValueShort other = (ValueShort) v;
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(value * other.value);
        }                 
        return ValueShort.get((short) (value * other.value));
    }

    public Value divide(Value v) throws SQLException {
        ValueShort other = (ValueShort) v;
        if (other.value == 0) {
            throw Message.getSQLException(ErrorCode.DIVISION_BY_ZERO_1, getSQL());
        }
        return ValueShort.get((short) (value / other.value));
    }

    public String getSQL() {
        return getString();
    }

    public int getType() {
        return Value.SHORT;
    }

    public short getShort() {
        return value;
    }

    protected int compareSecure(Value o, CompareMode mode) {
        ValueShort v = (ValueShort) o;
        if (value == v.value) {
            return 0;
        }
        return value > v.value ? 1 : -1;
    }

    public String getString() {
        return String.valueOf(value);
    }

    public long getPrecision() {
        return PRECISION;
    }

    public int hashCode() {
        return value;
    }

    public Object getObject() {
        return ObjectUtils.getShort(value);
    }

    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        prep.setShort(parameterIndex, value);
    }

    public static ValueShort get(short i) {
        return (ValueShort) Value.cache(new ValueShort(i));
    }
    
    public int getDisplaySize() {
        return DISPLAY_SIZE;
    }    
    
    protected boolean isEqual(Value v) {
        return v instanceof ValueShort && value == ((ValueShort) v).value;
    }

}
