/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jdbcx;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

//#ifdef JDK14
import java.io.Serializable;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
//#endif

import org.h2.jdbc.JdbcConnection;
import org.h2.message.TraceObject;

//#ifdef JDK16
/*
import org.h2.message.Message;
*/
//#endif

/**
 * A data source for H2 database connections. It is a factory for XAConnection
 * and Connection objects. This class is usually registered in a JNDI naming
 * service. To create a data source object and register it with a JNDI service,
 * use the following code:
 * 
 * <pre>
 * import org.h2.jdbcx.JdbcDataSource;
 * import javax.naming.Context;
 * import javax.naming.InitialContext;
 * JdbcDataSource ds = new JdbcDataSource();
 * ds.setURL(&quot;jdbc:h2:&tilde;/test&quot;);
 * ds.setUser(&quot;sa&quot;);
 * ds.setPassword(&quot;sa&quot;);
 * Context ctx = new InitialContext();
 * ctx.bind(&quot;jdbc/dsName&quot;, ds);
 * </pre>
 * 
 * To use a data source that is already registered, use the following code:
 * 
 * <pre>
 * import java.sql.Connection;
 * import javax.sql.DataSource;
 * import javax.naming.Context;
 * import javax.naming.InitialContext;
 * Context ctx = new InitialContext();
 * DataSource ds = (DataSource) ctx.lookup(&quot;jdbc/dsName&quot;);
 * Connection conn = ds.getConnection();
 * </pre>
 * 
 * In this example the user name and password are serialized as
 * well; this may be a security problem in some cases.
 */
public class JdbcDataSource extends TraceObject
//#ifdef JDK14
implements XADataSource, DataSource, ConnectionPoolDataSource, Serializable, Referenceable
//#endif
{

    private static final long serialVersionUID = 1288136338451857771L;

    private transient JdbcDataSourceFactory factory;
    private transient PrintWriter logWriter;
    private int loginTimeout;
    private String user = "";
    private String password = "";
    private String url = "";

    static {
        org.h2.Driver.load();
    }

    /**
     * The public constructor.
     */
    public JdbcDataSource() {
        initFactory();
        int id = getNextId(TraceObject.DATA_SOURCE);
        setTrace(factory.getTrace(), TraceObject.DATA_SOURCE, id);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        initFactory();
        in.defaultReadObject();
    }

    private void initFactory() {
        factory = new JdbcDataSourceFactory();
    }

    /**
     * Get the login timeout in seconds, 0 meaning no timeout.
     *
     * @return the timeout in seconds
     */
    public int getLoginTimeout() throws SQLException {
        debugCodeCall("getLoginTimeout");
        return loginTimeout;
    }

    /**
     * Set the login timeout in seconds, 0 meaning no timeout.
     * The default value is 0.
     * This value is ignored by this database.
     *
     * @param timeout the timeout in seconds
     */
    public void setLoginTimeout(int timeout) throws SQLException {
        debugCodeCall("setLoginTimeout", timeout);
        this.loginTimeout = timeout;
    }

    /**
     * Get the current log writer for this object.
     *
     * @return the log writer
     */
    public PrintWriter getLogWriter() throws SQLException {
        debugCodeCall("getLogWriter");
        return logWriter;
    }

    /**
     * Set the current log writer for this object.
     * This value is ignored by this database.
     *
     * @param out the log writer
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        debugCodeCall("setLogWriter(out)");
        logWriter = out;
    }

    /**
     * Open a new connection using the current URL, user name and password.
     *
     * @return the connection
     */
    public Connection getConnection() throws SQLException {
        debugCodeCall("getConnection");
        return getJdbcConnection(user, password);
    }

    /**
     * Open a new connection using the current URL and the specified user name
     * and password.
     * 
     * @param user the user name
     * @param password the password
     * @return the connection
     */
    public Connection getConnection(String user, String password) throws SQLException {
        debugCode("getConnection("+quote(user)+", "+quote(password)+");");
        return getJdbcConnection(user, password);
    }

    private JdbcConnection getJdbcConnection(String user, String password) throws SQLException {
        debugCode("getJdbcConnection("+quote(user)+", "+quote(password)+");");
        Properties info = new Properties();
        info.setProperty("user", user);
        info.setProperty("password", password);
        return new JdbcConnection(url, info);
    }

    /**
     * Get the current URL.
     *
     * @return the URL
     */
    public String getURL() {
        debugCodeCall("getURL");
        return url;
    }

    /**
     * Set the current URL.
     *
     * @param url the new URL
     */
    public void setURL(String url) {
        debugCodeCall("setURL", url);
        this.url = url;
    }

    /**
     * Set the current password
     *
     * @param password the new password.
     */
    public void setPassword(String password) {
        debugCodeCall("setPassword", password);
        this.password = password;
    }

    /**
     * Get the current password.
     *
     * @return the password
     */
    public String getPassword() {
        debugCodeCall("getPassword");
        return password;
    }

    /**
     * Get the current user name.
     *
     * @return the user name
     */
    public String getUser() {
        debugCodeCall("getUser");
        return user;
    }

    /**
     * Set the current user name.
     *
     * @param user the new user name
     */
    public void setUser(String user) {
        debugCodeCall("setUser", user);
        this.user = user;
    }

    /**
     * Get a new reference for this object, using the current settings.
     *
     * @return the new reference
     */
//#ifdef JDK14
    public Reference getReference() throws NamingException {
        debugCodeCall("getReference");
        String factoryClassName = JdbcDataSourceFactory.class.getName();
        Reference ref = new Reference(getClass().getName(), factoryClassName, null);
        ref.add(new StringRefAddr("url", url));
        ref.add(new StringRefAddr("user", user));
        ref.add(new StringRefAddr("password", password));
        ref.add(new StringRefAddr("loginTimeout", String.valueOf(loginTimeout)));
        return ref;
    }
//#endif

    /**
     * Open a new XA connection using the current URL, user name and password.
     *
     * @return the connection
     */
//#ifdef JDK14
    public XAConnection getXAConnection() throws SQLException {
        debugCodeCall("getXAConnection");
        int id = getNextId(XA_DATA_SOURCE);
        return new JdbcXAConnection(factory, id, url, user, password);
    }
//#endif

    /**
     * Open a new XA connection using the current URL and the specified user
     * name and password.
     * 
     * @param user the user name
     * @param password the password
     * @return the connection
     */
//#ifdef JDK14
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        debugCode("getXAConnection("+quote(user)+", "+quote(password)+");");
        int id = getNextId(XA_DATA_SOURCE);
        return new JdbcXAConnection(factory, id, url, user, password);
    }
//#endif

    /**
     * Open a new XA connection using the current URL, user name and password.
     *
     * @return the connection
     */
//#ifdef JDK14
    public PooledConnection getPooledConnection() throws SQLException {
        debugCodeCall("getPooledConnection");
        return getXAConnection();
    }
//#endif

    /**
     * Open a new XA connection using the current URL and the specified user
     * name and password.
     * 
     * @param user the user name
     * @param password the password
     * @return the connection
     */
//#ifdef JDK14
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        debugCode("getPooledConnection("+quote(user)+", "+quote(password)+");");
        return getXAConnection(user, password);
    }
//#endif

    /**
     * [Not supported] Return an object of this class if possible.
     *
     * @param iface the class
     */
//#ifdef JDK16
/*
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw Message.getUnsupportedException();
    }
*/
//#endif

    /**
     * [Not supported] Checks if unwrap can return an object of this class.
     *
     * @param iface the class
     */
//#ifdef JDK16
/*
    public boolean isWrapperFor(Class< ? > iface) throws SQLException {
        throw Message.getUnsupportedException();
    }
*/
//#endif

    /**
     * INTERNAL
     */
    public String toString() {
        return getTraceObjectName() + ": url=" + url + " user=" + user;
    }

}
