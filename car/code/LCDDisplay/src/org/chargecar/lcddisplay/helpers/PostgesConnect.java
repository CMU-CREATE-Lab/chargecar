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
    //[table name, num_colums]
    private Map<String, Integer> tables = new HashMap<String, Integer>();

    public PostgesConnect() {
        // connect to the database
        LOG.trace("Connecting to postgres db....");
        this.conn = connectToDatabaseOrDie();
        LOG.trace("Connected to postgres db....");
        LOG.trace("Getting information about each table....");
        getTableInfo();
    }

    public List makeQuery(final Connection conn, final String tableName, final String statement) {
        final List<String> results = new ArrayList<String>();
        int numColumns = 0;
        try {
            final Statement st = conn.createStatement();
            final ResultSet rs = st.executeQuery(statement);
            while (rs.next()) {
                numColumns = tables.get(tableName);
                for (int i = 0; i < numColumns; i++)
                    results.add(rs.getString(i + 1));
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            LOG.error("Threw a SQLException creating a list of the query results: " + se.getMessage());
        }
        return results;
    }

    public List makeQuery(final Connection conn, final int numCols, final String statement) {
        final List<String> results = new ArrayList<String>();
        try {
            final Statement st = conn.createStatement();
            final ResultSet rs = st.executeQuery(statement);
            while (rs.next()) {

                for (int i = 0; i < numCols; i++)
                    results.add(rs.getString(i + 1));
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            LOG.error("Threw a SQLException creating a list of the query results: " + se.getMessage());
        }
        return results;
    }

    private Connection connectToDatabaseOrDie() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            final String url = "jdbc:postgresql://localhost/template_postgis15";
            conn = DriverManager.getConnection(url, "postgres", "test");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(2);
        }
        return conn;
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
            LOG.error("Threw a SQLException creating the list of table info: " + se.getMessage());
        }
    }

    public Connection getConn() {
        return conn;
    }


}

