package org.chargecar.lcddisplay.helpers;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

public class PostgesConnect {
    private static final Logger LOG = Logger.getLogger(PostgesConnect.class);
    private Connection conn = null;
    private Map<String, Integer> tables = new HashMap<String, Integer>(); //[tableName, numColums]
    private static final String database = "template_postgis15";
    private static final String databaseUserName = "postgres";
    private static final String databasePassword = "test";

    public PostgesConnect() {
        // connect to the database
        LOG.trace("Connecting to postgres db....");
        this.conn = connectToDatabaseOrDie();
        LOG.trace("Connected to postgres db....");
        LOG.trace("Getting information about each table....");
        //get table names and number of columns in each table
        getTableInfo();
    }

    public List<List> makeQuery(final Connection conn, final String tableName, final String statement) {
        if (conn == null || tables.get(tableName) == null || statement == null) return null;
        return makeQuery(conn, tables.get(tableName), statement);
    }

    public List<List> makeQuery(final Connection conn, final int numCols, final String statement) {
        if (conn == null || statement == null) return null;
        final List<List> results = new ArrayList<List>();
        try {
            final Statement st = conn.createStatement();
            final ResultSet rs = st.executeQuery(statement);
            final List<String> columns = new ArrayList<String>(numCols);
            while (rs.next()) {
                for (int i = 0; i < numCols; i++)
                    columns.add(rs.getString(i + 1)); //db starts numbering at 1
                results.add(columns);
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            LOG.error("Threw a SQLException while creating a list of the query results: " + se);
        }
        return results;
    }

    private Connection connectToDatabaseOrDie() {
        Connection tmpConn = null;
        try {
            Class.forName("org.postgresql.Driver"); //ensure the jdbc driver exists in path
            final String url = "jdbc:postgresql://localhost/"+database;
            tmpConn = DriverManager.getConnection(url, databaseUserName, databasePassword);
        } catch (ClassNotFoundException e) {
            LOG.trace(e);
            System.exit(1);
        } catch (SQLException e) {
            LOG.trace(e);
            System.exit(2);
        }
        return tmpConn;
    }

    private void getTableInfo() {
        try {
            final Statement st = conn.createStatement();
            final ResultSet rs = st.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';");

            while (rs.next()) {
                final String tableName = rs.getString("table_name");
                final Statement st2 = conn.createStatement();
                final ResultSet rs2 = st2.executeQuery("select count(*) from information_schema.columns where table_name=" + "'" + tableName + "'" + ";");
                while (rs2.next()) {
                    tables.put(tableName, rs2.getInt("count"));
                }
                rs2.close();
                st2.close();
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            LOG.error("Threw a SQLException while creating the list of table info: " + se);
        }
    }

    public Connection getConn() {
        return conn;
    }
}
