/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.engine;

/**
 * Constants are fixed values that are used in the whole database code.
 */
public class Constants {

    /**
     * The build id is incremented for each public release.
     */
    public static final int BUILD_ID = 137;

    /**
     * The build id of the last stable release.
     */
    public static final int BUILD_ID_STABLE = 136;

    /**
     * If H2 is compiled to be included in a product, this should be set to
     * a unique vendor id (to distinguish from official releases).
     * Additionally, a version number should be set to distinguish releases.
     * Example: ACME_SVN1651_BUILD3
     */
    public static final String BUILD_VENDOR_AND_VERSION = null;

    /**
     * The build date is updated for each public release.
     */
    public static final String BUILD_DATE = "2010-06-06";

    /**
     * The build date is updated for each public release.
     */
    public static final String BUILD_DATE_STABLE = "2010-05-24";

    /**
     * The TCP protocol version number. This protocol is used by the TCP
     * server and remote JDBC client.
     */
    public static final int TCP_PROTOCOL_VERSION = 6;

    /**
     * The major version of this database.
     */
    public static final int VERSION_MAJOR = 1;

    /**
     * The minor version of this database.
     */
    public static final int VERSION_MINOR = 2;

    /**
     * Constant meaning both numbers and text is allowed in SQL statements.
     */
    public static final int ALLOW_LITERALS_ALL = 2;

    /**
     * Constant meaning no literals are allowed in SQL statements.
     */
    public static final int ALLOW_LITERALS_NONE = 0;

    /**
     * Constant meaning only numbers are allowed in SQL statements (but no
     * texts).
     */
    public static final int ALLOW_LITERALS_NUMBERS = 1;

    /**
     * The minimum number of entries to keep in the cache.
     */
    public static final int CACHE_MIN_RECORDS = 16;

    /**
     * The value of the cluster setting if clustering is disabled.
     */
    public static final String CLUSTERING_DISABLED = "''";

    /**
     * The database URL used when calling a function if only the column list
     * should be returned.
     */
    public static final String CONN_URL_COLUMNLIST = "jdbc:columnlist:connection";

    /**
     * The database URL used when calling a function if the data should be
     * returned.
     */
    public static final String CONN_URL_INTERNAL = "jdbc:default:connection";

    /**
     * The cost is calculated on rowcount + this offset,
     * to avoid using the wrong or no index if the table
     * contains no rows _currently_ (when preparing the statement)
     */
    public static final int COST_ROW_OFFSET = 1000;

    /**
     * The number of milliseconds after which to check for a deadlock if locking
     * is not successful.
     */
    public static final int DEADLOCK_CHECK = 100;

    /**
     * The default port number of the HTTP server (for the H2 Console).
     * This value is also in the documentation and in the Server javadoc.
     */
    public static final int DEFAULT_HTTP_PORT = 8082;

    /**
     * The default value for the maximum transaction log size.
     */
    public static final long DEFAULT_MAX_LOG_SIZE = 2 * 1024 * 1024;

    /**
     * The default maximum length on an in-memory LOB object.
     * Larger objects will be written to a temporary file.
     */
    public static final int DEFAULT_MAX_LENGTH_CLIENTSIDE_LOB = 65536;

    /**
     * The default maximum number of rows to be kept in memory in a result set.
     */
    public static final int DEFAULT_MAX_MEMORY_ROWS = 10000;

    /**
     * The default port of the TCP server.
     * This port is also used in the documentation and in the Server javadoc.
     */
    public static final int DEFAULT_TCP_PORT = 9092;

    /**
     * The default delay in milliseconds before the transaction log is written.
     */
    public static final int DEFAULT_WRITE_DELAY = 500;

    /**
     * The password is hashed this many times
     * to slow down dictionary attacks.
     */
    public static final int ENCRYPTION_KEY_HASH_ITERATIONS = 1024;

    /**
     * The block of a file. It is also the encryption block size.
     */
    public static final int FILE_BLOCK_SIZE = 16;

    /**
     * For testing, the lock timeout is smaller than for interactive use cases.
     * This value could be increased to about 5 or 10 seconds.
     */
    public static final int INITIAL_LOCK_TIMEOUT = 2000;

    /**
     * The block size for I/O operations.
     */
    public static final int IO_BUFFER_SIZE = 4 * 1024;

    /**
     * The block size used to compress data in the LZFOutputStream.
     */
    public static final int IO_BUFFER_SIZE_COMPRESS = 128 * 1024;

    /**
     * The lock mode that means no locking is used at all.
     */
    public static final int LOCK_MODE_OFF = 0;

    /**
     * The lock mode that means read locks are acquired, but they are released
     * immediately after the statement is executed.
     */
    public static final int LOCK_MODE_READ_COMMITTED = 3;

    /**
     * The lock mode that means table level locking is used for reads and
     * writes.
     */
    public static final int LOCK_MODE_TABLE = 1;

    /**
     * The lock mode that means table level locking is used for reads and
     * writes. If a table is locked, System.gc is called to close forgotten
     * connections.
     */
    public static final int LOCK_MODE_TABLE_GC = 2;

    /**
     * The number of milliseconds to wait between checking the .lock.db file
     * still exists once a database is locked.
     */
    public static final int LOCK_SLEEP = 1000;

    /**
     * The highest possible parameter index.
     */
    public static final int MAX_PARAMETER_INDEX = 100000;

    /**
     * The number of bytes in random salt that is used to hash passwords.
     */
    public static final int SALT_LEN = 8;

    /**
     * The database URL prefix of this database.
     */
    public static final String START_URL = "jdbc:h2:";

    /**
     * The name prefix used for indexes that are not explicitly named.
     */
    public static final String PREFIX_INDEX = "INDEX_";

    /**
     * The name prefix used for primary key constraints that are not explicitly
     * named.
     */
    public static final String PREFIX_PRIMARY_KEY = "PRIMARY_KEY_";

    /**
     * Every user belongs to this role.
     */
    public static final String PUBLIC_ROLE_NAME = "PUBLIC";

    /**
     * The name of the default schema.
     */
    public static final String SCHEMA_MAIN = "PUBLIC";

    /**
     * The default selectivity (used if the selectivity is not calculated).
     */
    public static final int SELECTIVITY_DEFAULT = 50;

    /**
     * The number of distinct values to keep in memory when running ANALYZE.
     */
    public static final int SELECTIVITY_DISTINCT_COUNT = 10000;

    /**
     * The name of the server properties file.
     */
    public static final String SERVER_PROPERTIES_FILE = ".h2.server.properties";

    /**
     * Queries that take longer than this number of milliseconds are written to
     * the trace file with the level info.
     */
    public static final long SLOW_QUERY_LIMIT_MS = 100;

    /**
     * The file name suffix of page files.
     */
    public static final String SUFFIX_PAGE_FILE = ".h2.db";

    /**
     * The file name suffix of all database files.
     */
    public static final String SUFFIX_DB_FILE = ".db";

    /**
     * The file name suffix of file lock files that are used to make sure a
     * database is open by only one process at any time.
     */
    public static final String SUFFIX_LOCK_FILE = ".lock.db";

    /**
     * The file name suffix of large object files.
     */
    public static final String SUFFIX_LOB_FILE = ".lob.db";

    /**
     * The suffix of the directory name used if LOB objects are stored in a
     * directory.
     */
    public static final String SUFFIX_LOBS_DIRECTORY = ".lobs.db";

    /**
     * The file name suffix of temporary files.
     */
    public static final String SUFFIX_TEMP_FILE = ".temp.db";

    /**
     * The file name suffix of trace files.
     */
    public static final String SUFFIX_TRACE_FILE = ".trace.db";

    /**
     * The delay that is to be used if throttle has been enabled.
     */
    public static final int THROTTLE_DELAY = 50;

    /**
     * The maximum size of an undo log block.
     */
    public static final int UNDO_BLOCK_SIZE = 1024 * 1024;

    /**
     * The database URL format in simplified Backus-Naur form.
     */
    public static final String URL_FORMAT = START_URL +
    "{ {.|mem:}[name] | [file:]fileName | {tcp|ssl}:[//]server[:port][,server2[:port]]/name }[;key=value...]";

    /**
     * Name of the character encoding format.
     */
    public static final String UTF8 = "UTF8";

    /**
     * The maximum time in milliseconds to keep the cost of a view.
     * 10000 means 10 seconds.
     */
    public static final int VIEW_COST_CACHE_MAX_AGE = 10000;

    /**
     * The name of the index cache that is used for temporary view (subqueries
     * used as tables).
     */
    public static final int VIEW_INDEX_CACHE_SIZE = 64;

    /**
     * Whether searching in Blob values should be supported.
     */
    public static final boolean BLOB_SEARCH = false;

    /**
     * The package name of user defined classes.
     */
    public static final String USER_PACKAGE = "org.h2.dynamic";

    private Constants() {
        // utility class
    }

    /**
     * Get the version of this product, consisting of major version, minor version,
     * and build id.
     *
     * @return the version number
     */
    public static String getVersion() {
        String version = VERSION_MAJOR + "." + VERSION_MINOR + "." + BUILD_ID;
        if (BUILD_VENDOR_AND_VERSION != null) {
            version += "_" + BUILD_VENDOR_AND_VERSION;
        }
        return version;
    }

    /**
     * Get the last stable version name.
     *
     * @return the version number
     */
    public static Object getVersionStable() {
        return "1.2." + BUILD_ID_STABLE;
    }

    /**
     * Get the complete version number of this database, consisting of
     * the major version, the minor version, the build id, and the build date.
     *
     * @return the complete version
     */
    public static String getFullVersion() {
        return getVersion() + " (" + BUILD_DATE + ")";
    }

}
