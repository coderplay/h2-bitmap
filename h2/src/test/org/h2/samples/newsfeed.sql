/*
 * Copyright 2004-2008 H2 Group. Multiple-Licensed under the H2 License, 
 * Version 1.0, and under the Eclipse Public License, Version 1.0 
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

CREATE TABLE CHANNEL(TITLE VARCHAR, LINK VARCHAR, DESC VARCHAR,
    LANGUAGE VARCHAR, PUB TIMESTAMP, LAST TIMESTAMP, AUTHOR VARCHAR);

INSERT INTO CHANNEL VALUES('H2 Database Engine' ,
    'http://www.h2database.com', 'H2 Database Engine', 'en-us', NOW(), NOW(), 'Thomas Mueller');

CREATE TABLE ITEM(ID INT PRIMARY KEY, TITLE VARCHAR, ISSUED TIMESTAMP, DESC VARCHAR);

INSERT INTO ITEM VALUES(50,
'New version available: 1.1.100 (beta; 2008-10-04)', '2008-10-04 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>In version H2 1.1, some features are now enabled by default.
</li><li>New auto-reconnect feature. 
    To enable, append ;AUTO_RECONNECT=TRUE to the database URL. 
<ul><li>The H2 Console tool now works with the JDBC-ODBC bridge.
</li><li>The H2 Console tool now supports command line options.
</li><li>The h2console.war can now be built using the Java build.
</li><li>If you want that each connection opens its own database, append 
    ;OPEN_NEW=TRUE to the database URL.
</li><li>CreateCluster: the property 'serverlist' is now called 'serverList'.
</li><li>Databases names can now be one character long. 
</li></ul>
<b>Bugfixes:</b>
<ul><li>Connections from a local address other than 'localhost' were not allowed by default.
</li><li>Large objects did not work for in-memory databases in server mode in Linux.
</li><li>The ConvertTraceFile tool could not parse some files.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(49,
'New version available: 1.0.79 (2008-09-26)', '2008-09-26 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>Row level locking for MVCC is now enabled.
</li><li>Multiple processes can now access the same database by appending 
    ;AUTO_SERVER=TRUE to the database URL. 
</li><li>The database supports the SHOW command for better MySQL and PostgreSQL compatibility.
</li><li>Result sets with just a unique index can now be updated.
</li><li>Linked tables can now share the connection.
</li><li>Linked tables can now be read-only.
</li><li>Linked tables: the schema name can now be set.
</li><li>Linked tables: worked around a bug in Oracle with the CHAR data type.
</li><li>Temporary linked tables are now supported.
</li><li>Faster storage re-use algorithm thanks to Greg Dhuse from cleversafe.com.
</li><li>Faster hash code calculation for large binary arrays.
</li><li>Multi-Version Concurrency may no longer be used when using 
    the multi-threaded kernel feature.
</li><li>The H2 Console now abbreviates large texts in results.
</li><li>SET SCHEMA_SEARCH_PATH is now documented.
</li><li>Can now start a TCP server with port 0 (automatically select a port).
</li><li>The server tool now displays the correct IP address if networked.
</li></ul>
<b>Bugfixes:</b>
<ul><li>Multiple UNION queries could not be used in derived tables. 
</li><li>It was possible to create tables in read-only databases.
</li><li>SET SCHEMA did not work for views.
</li><li>The maximum log file size setting was ignored for large databases.
</li><li>The data type JAVA_OBJECT could not be used in updatable result sets.
</li><li>The system property h2.optimizeInJoin did not work correctly.
</li><li>Conditions such as ID=? AND ID&gt;? were slow.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(48,
'New version available: 1.0.78 (2008-08-28)', '2008-08-28 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>Column aliases can now be used in GROUP BY and HAVING.
</li><li>Java methods with variable number of parameters can now be used (for Java 1.5 or newer).
</li><li>The build target 'build jarSmall' now includes the embedded database.
</li><li>JdbcDataSource now keeps the password in a char array where possible.
</li><li>Jason Brittain has contributed MySQL date functions. Thanks a lot!
    They are not in the h2.jar file currently, but in src/tools/org/h2/mode/FunctionsMySQL.java.
    To install, add this class to the classpath and call FunctionsMySQL.register(conn) in the Java code.
</li><li>The Japanese translation has been improved by Masahiro Ikemoto. Thanks a lot!
</li><li>The documentation no longer uses a frameset (except the Javadocs).
</li></ul>
<b>Bugfixes:</b>
<ul><li>The H2 Console replaced an empty user name with a single space. 
</li><li>ResultSet.absolute did not always work with large result sets.
</li><li>When using DB_CLOSE_DELAY, sometimes a NullPointerException is thrown when
    the database is opened almost at the same time as it is closed automatically.
    Thanks a lot to Dmitry Pekar for finding this!
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);
    
INSERT INTO ITEM VALUES(47,
'New version available: 1.0.77 (2008-08-16)', '2008-08-16 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>JaQu is now using prepared statements and supports Date, Time, Timestamp.
</li><li>Support a comma before closing a list, as in: create table test(id int,)
</li><li>DB2 compatibility: the DB2 fetch-first-clause is supported.
</li><li>ResultSet.setFetchSize is now supported.
</li></ul>
<b>Bugfixes:</b>
<ul><li>When using remote in-memory databases, large LOB objects did not work.
</li><li>Timestamp columns such as TIMESTAMP(6) were not compatible to other database. 
</li><li>Opening a large database was slow if there was a problem opening the previous time.
</li><li>Oracle compatibility: old style outer join syntax using (+) did work correctly sometimes.
</li><li>MySQL compatibility: linked tables had lower case column names on some systems. 
</li><li>NOT IN(SELECT ...) was incorrect if the subquery returns no rows.
</li><li>CREATE TABLE AS SELECT did not work correctly in the multi-version concurrency mode.
</li><li>It has been reported that when using Install4j on some Linux systems and enabling the 'pack200' option, 
    the h2.jar becomes corrupted by the install process, causing application failure. 
    A workaround is to add an empty file h2.jar.nopack next to the h2.jar file. 
    The reason for this problem is not known.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);
    
INSERT INTO ITEM VALUES(46,
'New version available: 1.0.76 (2008-07-27)', '2008-07-27 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>Key values can now be changed in updatable result sets.
</li><li>Changes in updatable result sets are now always visible.
</li><li>There is a problem with Hibernate when using Boolean columns, see 
    http://opensource.atlassian.com/projects/hibernate/browse/HHH-3401
</li><li>The comment of a domain (user defined data type) is now used.
</li></ul>
<b>Bugfixes:</b>
<ul><li>ResultSetMetaData.getColumnClassName now returns the correct 
    class name for BLOB and CLOB.
</li><li>Fixed the Oracle mode: Oracle allows multiple rows only where 
    all columns of the unique index are NULL. 
</li><li>ORDER BY on tableName.columnName didn't work correctly if the column 
    name was also used as an alias.
</li><li>Invalid database names are now detected and a better error message is thrown.
</li><li>H2 Console: The progress display when opening a database has been improved.
</li><li>The error message when the server doesn't start has been improved.
</li><li>Temporary files were sometimes deleted too late when executing large insert, update, 
    or delete operations.
</li><li>The database file was growing after deleting many rows, and after large update operations.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(45,
'New version available: 1.0.75 (2008-07-14)', '2008-07-14 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>The JaQu (Java Query) tool has been improved.
</li><li>The H2 Console can be started with an open connection to inspect a database while debugging.
</li><li>The referential constraint checking performance has been improvement.
</li></ul>
<b>Bugfixes:</b>
<ul><li>Running out of memory could result in incomplete transactions or corrupted databases. Fixed.
</li><li>CSVREAD did not process NULL correctly when using a whitespace field separator.
</li><li>Stopping a WebServer didn't always work. Fixed.
</li><li>Sometimes, order by in a query that uses the same table multiple times didn't work.
</li><li>A multi version concurrency (MVCC) problem has been fixed.
</li><li>Some views with multiple joined tables didn't work.
</li><li>The Oracle mode now allows multiple rows with NULL in a unique index.
</li><li>Some database metadata calls returned the wrong data type for DATA_TYPE columns.
</li><li>A bug int the Lucene fulltext implementation has been fixed.
</li><li>The character '$' could not be used in identifier names.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(44,
'New version available: 1.0.74 (2008-06-21)', '2008-06-21 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>JaQu (Java Query), a tool similar to LINQ (Language Integrated Query) 
    is now included. See also
    <a href="http://code.google.com/p/h2database/source/browse/trunk/h2/src/test/org/h2/test/jaqu/SamplesTest.java">
    code examples</a>.
</li><li>Support for overloaded Java methods. Many thanks to Gary Tong!
</li><li>Deadlocks are now detected.
</li><li>Linked tables: statements executed against the target are list with trace level 3.
</li><li>RunScript tool: new options to show and check the results of queries.
</li><li>Improved compatibility with databases that only allow one row with 'NULL' in a unique 
    index. Use the compatibility mode to enable this feature.
</li><li>The source code is now switched to Java 1.6 by default.
</li><li>The ChangePassword tool is now called ChangeFileEncryption.
</li><li>It is no longer allowed to create columns with the data type NULL.
</li></ul>
<b>Bugfixes:</b>
<ul><li>The Lucene fulltext index was always re-created when opening a database.
</li><li>Setting a column default with a different data type did not work.
</li><li>Opening big databases was sometimes very slow. Fixed.
</li><li>RUNSCRIPT could throw a NullPointerException.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(43,
'New version available: 1.0.73 (2008-05-31)', '2008-05-31 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>ParameterMetaData now returns the right data type for most cases.
</li><li>New column INFORMATION_SCHEMA.CONSTRAINTS.UNIQUE_INDEX_NAME.
</li><li>Some SET statements no longer commit a transaction. 
</li><li>The table SYSTEM_RANGE now supports parameters.
</li><li>The SCRIPT command does now emit IF NOT EXISTS for CREATE ROLE.
</li><li>Improved MySQL compatibility for AUTO_INCREMENT columns.
</li><li>The aggregate functions BOOL_OR and BOOL_AND are now supported.
</li><li>Negative scale values are now supported.
</li><li>Infinite numbers and NaN are now better supported.
</li><li>The fulltext search now supports CLOB.
</li><li>A right can now be granted multiple times.
</li></ul>
<b>Bugfixes:</b>
<ul><li>Disconnecting or unmounting drives while the database is open 
    now throws the right exception.
</li><li>The H2 Console could not be shut down from within the tool.
</li><li>If the password was passed as a char array, it was kept in an internal buffer
        longer than required. Theoretically the password could have been stolen
        if the main memory was swapped to disk before the garbage collection was run.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(42,
'New version available: 1.0.72 (2008-05-10)', '2008-05-10 12:00:00',
$$A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
<b>Changes and new functionality:</b>
<ul><li>SLF4J is now supported by using adding TRACE_LEVEL_FILE=4
        to the database URL.
</li><li>A subset of the PostgreSQL 'dollar quoting' feature is now supported.
</li><li>Updates made to updatable rows are now visible within the same result set. 
        DatabaseMetaData.ownUpdatesAreVisible now returns true.
</li><li>ParameterMetaData now returns the correct data 
        for INSERT and UPDATE statements.
</li><li>Shell tool: DESCRIBE now supports an schema name.
</li><li>The Shell tool now uses java.io.Console to read the password
        when using JDK 1.6
</li><li>The Japanese translation of the error messages and the H2 Console 
        has been completed by Masahiro Ikemoto (Arizona Design Inc.)
</li><li>Statements can now be canceled remotely 
        (when using remote connections).
</li><li>Triggers are no longer executed when executing an changing the table
        structure (ALTER TABLE).
</li></ul>
<b>Bugfixes:</b>
<ul><li>Some databases could not be opened when appending 
        ;RECOVER=1 to the database URL.
</li><li>The recovery tool did not work if the table name contained spaces
        or if there was a comment on the table.
</li><li>When setting BLOB or CLOB values larger than 65 KB using 
        a remote connection, temporary files were kept on the client
        longer than required (until the connection was closed or the 
        object is garbage collected). Now they are removed as soon
        as the PreparedStatement is closed, or when the value is
        overwritten.
</li><li>When using read-only databases and setting LOG=2, an exception
        was written to the trace file when closing the database. Fixed.
</li></ul>
For details, see the 'Change Log' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the 'Roadmap' page at
http://www.h2database.com/html/roadmap.html
$$);

INSERT INTO ITEM VALUES(41,
'New version available: 1.0.71 (2008-04-25)', '2008-04-25 12:00:00',
'A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click ''Refresh'').
<br />
<b>Changes and new functionality:</b>
<ul><li>H2 is now dual-licensed under the Eclipse Public License (EPL) and the
    old ''H2 License'' (which is basically MPL).
</li><li>New traditional Chinese translation. Thanks a lot to Derek Chao!
</li></ul>
<b>Bugfixes:</b>
<ul><li>Sometimes an exception ''File ID mismatch'' or ''try to add a record twice''
    occurred after large records (8 KB or larger) are updated or deleted.
    See also http://code.google.com/p/h2database/issues/detail?id=22
</li><li>H2 Console: The tools can now be translated 
    (it didn''t work in the last release).
</li><li>Indexes were not used when enabling the optimization for 
    IN(SELECT...) (system property h2.optimizeInJoin).
</li></ul>
For details, see the ''Change Log'' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the ''Roadmap'' page at
http://www.h2database.com/html/roadmap.html
');

INSERT INTO ITEM VALUES(40,
'New version available: 1.0.70 (2008-04-20)', '2008-04-20 12:00:00',
'A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click ''Refresh'').
<br />
<b>Changes and new functionality:</b>
<ul><li>The plan is to dual-license H2. The additional license is EPL (Eclipse Public License).
    The current license (MPL, Mozilla Public License) will stay.
    Current users are not affected because they can keep MPL. 
    EPL is very similar to MPL, the only bigger difference is related to patents
    (EPL is a bit more business friendly in this regard).
    See also http://opensource.org/licenses/eclipse-1.0.php,
    http://www.eclipse.org/legal/eplfaq.php (FAQ),
    http://blogs.zdnet.com/Burnette/?p=131
</li><li>The ConvertTraceFile tool now generates SQL statement statistics.
</li><li>New system property h2.enableAnonymousSSL (default: true).
</li><li>The precision if SUBSTR is now calculated if possible.    
</li><li>The autocomplete in the H2 Console has been improved a bit.
</li><li>The tools in the H2 Console are now translatable.
</li><li>The servlet and lucene jar files are now automatically downloaded when building.
</li><li>The code switch tool has been replaced by a simpler tool. 
</li><li>Started to write a Ant replacement (''JAnt'') that uses pure Java 
    build definitions.Future plan: support creating custom h2 
    distributions (for embedded use). Maybe create a new project ''Jant''
    or ''Javen'' if other people are interested.
</li><li>The jar file is now about 10% smaller because the variable debugging info 
    is no longer included.
</li><li>Added shell scripts run.sh and build.sh.
</li><li>The Japanese translation of the error messages and the 
  H2 Console has been improved. Thanks a lot to Masahiro IKEMOTO. 
</li><li>Optimization for MIN() and MAX() when using MVCC.
</li><li>To protect against remote brute force password attacks, 
    the delay after each unsuccessful login now gets double as long.
</li><li>The built-in connection pool is not called JdbcConnectionPool. 
</li><li>Nested joins are now supported (A JOIN B JOIN C ON .. ON ..)
</li></ul>
<b>Bugfixes:</b>
<ul><li>Multi version concurrency (MVCC): when a row was updated, 
    and the updated column was not indexed, this update was visible sometimes 
    for other sessions even if it was not committed.
</li><li>Calling SHUTDOWN on one connection and starting a query on 
    another connection concurrently could result in a Java level deadlock.
</li><li>Databases in zip files: large queries are now supported.
</li><li>Invalid inline views threw confusing SQL exceptions.
</li><li>After setting the query timeout and then resetting it, the next query
    would still timeout. Fixed.
</li><li>Adding a IDENTITY column to a table with data threw a lock timeout.
</li><li>OutOfMemoryError could occur when using EXISTS or IN(SELECT ..).
</li></ul>
For details, see the ''Change Log'' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the ''Roadmap'' page at
http://www.h2database.com/html/roadmap.html
');

INSERT INTO ITEM VALUES(39,
'New version available: 1.0.69 (2008-03-29)', '2008-03-29 12:00:00',
'A new version of H2 is available for <a href="http://www.h2database.com">download</a>.
(You may have to click ''Refresh'').
<br />
<b>Changes and new functionality:</b>
<ul><li>Most command line tools can now be called within the H2 Console.
</li><li>A new Shell tools is now included to query a database from the command line.
</li><li>Some command line options have changed (the old behavior is still supported).
</li><li>New system property h2.sortNullsHigh to invert the default NULL sorting.
</li><li>ALTER TABLE or CREATE TABLE now support parameters.
</li><li>TRACE_LEVEL_ settings are no longer persistent.
</li></ul>
<b>Bugfixes:</b>
<ul><li>When a log file switch occurred in the middle of certain operations,
    the database could not be started normally (RECOVER=1 was required).
</li><li>Altering a sequence didn''t unlock the system table with autocommit disabled.
</li><li>CSVWRITE caused a NullPointerException when not specifying a nullString.
</li><li>Years below 1 were not supported correctly.
</li><li>The recovery tool didn''t work correctly for tables without rows.
</li><li>It is no longer possible to create a role with the name of an existing user.
</li><li>The memory usage of native fulltext search has been improved.
</li><li>Performance was very slow when using LOG=2. 
</li><li>The linear hash has been removed because it was slow and sometimes incorrect.
</li></ul>
For details, see the ''Change Log'' at
http://www.h2database.com/html/changelog.html
<br />
For future plans, see the ''Roadmap'' page at
http://www.h2database.com/html/roadmap.html
');

SELECT 'newsfeed-rss.xml' FILE,
    XMLSTARTDOC() ||
    XMLNODE('rss', XMLATTR('version', '2.0'),
        XMLNODE('channel', NULL,
            XMLNODE('title', NULL, C.TITLE) ||
            XMLNODE('link', NULL, C.LINK) ||
            XMLNODE('description', NULL, C.DESC) ||
            XMLNODE('language', NULL, C.LANGUAGE) ||
            XMLNODE('pubDate', NULL, FORMATDATETIME(C.PUB, 'EEE, d MMM yyyy HH:mm:ss z', 'en', 'GMT')) ||
            XMLNODE('lastBuildDate', NULL, FORMATDATETIME(C.LAST, 'EEE, d MMM yyyy HH:mm:ss z', 'en', 'GMT')) ||
            GROUP_CONCAT(
                XMLNODE('item', NULL,
                    XMLNODE('title', NULL, I.TITLE) ||
                    XMLNODE('link', NULL, C.LINK) ||
                    XMLNODE('description', NULL, XMLCDATA(I.TITLE))
                )
            ORDER BY I.ID DESC SEPARATOR '')
        )
    ) CONTENT
FROM CHANNEL C, ITEM I
UNION
SELECT 'newsfeed-atom.xml' FILE,
    XMLSTARTDOC() ||
    XMLNODE('feed', XMLATTR('version', '0.3') || XMLATTR('xmlns', 'http://purl.org/atom/ns#') || XMLATTR('xml:lang', C.LANGUAGE),
        XMLNODE('title', XMLATTR('type', 'text/plain') || XMLATTR('mode', 'escaped'), C.TITLE) ||
        XMLNODE('author', NULL, XMLNODE('name', NULL, C.AUTHOR)) ||
        XMLNODE('link', XMLATTR('rel', 'alternate') || XMLATTR('type', 'text/html') || XMLATTR('href', C.LINK), NULL) ||
        XMLNODE('modified', NULL, FORMATDATETIME(C.LAST, 'yyyy-MM-dd''T''HH:mm:ss.SSS', 'en', 'GMT')) ||
        GROUP_CONCAT(
            XMLNODE('entry', NULL,
                XMLNODE('title', XMLATTR('type', 'text/plain') || XMLATTR('mode', 'escaped'), I.TITLE) ||
                XMLNODE('link', XMLATTR('rel', 'alternate') || XMLATTR('type', 'text/html') || XMLATTR('href', C.LINK), NULL) ||
                XMLNODE('id', NULL, XMLTEXT(C.LINK || '/' || I.ID)) ||
                XMLNODE('issued', NULL, FORMATDATETIME(I.ISSUED, 'yyyy-MM-dd''T''HH:mm:ss.SSS', 'en', 'GMT')) ||
                XMLNODE('modified', NULL, FORMATDATETIME(I.ISSUED, 'yyyy-MM-dd''T''HH:mm:ss.SSS', 'en', 'GMT')) ||
                XMLNODE('content', XMLATTR('type', 'text/html') || XMLATTR('mode', 'escaped'), XMLCDATA(I.DESC))
            )
        ORDER BY I.ID DESC SEPARATOR '')
    ) CONTENT
FROM CHANNEL C, ITEM I
UNION
SELECT 'newsletter.txt' FILE, I.DESC CONTENT FROM ITEM I WHERE I.ID = (SELECT MAX(ID) FROM ITEM)
