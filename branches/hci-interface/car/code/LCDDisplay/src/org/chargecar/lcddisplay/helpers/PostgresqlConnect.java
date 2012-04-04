package org.chargecar.lcddisplay.helpers;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PostgresqlConnect {
    private static final Logger LOG = Logger.getLogger(PostgresqlConnect.class);
    private Connection conn = null;
    private Map<String, Integer> tables = new HashMap<String, Integer>(); //[tableName, numColums]
    private static final String defaultDatabase = "geodb";
    private static final String defaultDatabaseUserName = "postgres";
    private static final String defaultDatabasePassword = "chargecar";
    private String database;
    private String databaseUserName;
    private String databasePassword;
    private String dbPropertiesFileName = "postgresdb.properties";
    private final byte[] dataSynchronizationLock = new byte[0];
    private Properties savedProperties = null;

    private static class LazyHolder {
        private static final PostgresqlConnect INSTANCE = new PostgresqlConnect();

        private LazyHolder() {
        }
    }

    public static PostgresqlConnect getInstance() {
        try {
            return LazyHolder.INSTANCE;
        } catch (Throwable t) {
            LOG.error("PostgresqlConnect.getInstance(): " + t);
            return null;
        }
    }

    public Map<Object, Object> getPropertiesInstance() {
        synchronized (dataSynchronizationLock) {
            return Collections.unmodifiableMap(savedProperties);
        }
    }

    public String getSavedProperty(final String key) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) return null;
            return savedProperties.getProperty(key);
        }
    }

    public void setSavedProperty(final String key, final String value) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) return;
            savedProperties.setProperty(key, value);
        }
    }

    public String getCurrentPropertiesFileName() {
        synchronized (dataSynchronizationLock) {
            return dbPropertiesFileName;
        }
    }

    public void setCurrentPropertiesFileName(final String newPropertiesFileName) {
        synchronized (dataSynchronizationLock) {
            dbPropertiesFileName = newPropertiesFileName;
        }
    }

    public boolean openSavedProperties(final String fileName) {
        synchronized (dataSynchronizationLock) {
            savedProperties = new Properties();
            try {
                savedProperties.load(new FileInputStream(fileName));
            } catch (IOException e) {
                LOG.error("Error reading properties file: " + e);
                return false;
            }
            return true;
        }
    }

    public PostgresqlConnect() {
        //read in db properties file
        LOG.debug("Reading in postgres db properties file...");
        openSavedProperties(dbPropertiesFileName);
        database = getSavedProperty("database");
        databaseUserName = getSavedProperty("databaseUserName");
        databasePassword = getSavedProperty("databasePassword");
        //fill in default values if we failed to read in from the properties file
        if (database == null || databaseUserName == null || databasePassword == null) {
            database = defaultDatabase;
            databaseUserName = defaultDatabaseUserName;
            databasePassword = defaultDatabasePassword;
        }
        // connect to the database
        LOG.debug("Connecting to postgres db...");
        this.conn = connectToDatabase();
        LOG.debug("Connected to postgres db...");
        LOG.debug("Getting information about each table....");
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

    private Connection connectToDatabase() {
        final Connection tmpConn;
        try {
            Class.forName("org.postgresql.Driver"); //ensure the jdbc driver exists in path
            final String url = "jdbc:postgresql://localhost/" + database;
            tmpConn = DriverManager.getConnection(url, databaseUserName, databasePassword);
        } catch (ClassNotFoundException e) {
            LOG.error(e);
            return null;
        } catch (SQLException e) {
            LOG.error(e);
            return null;
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

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            LOG.error("Unable to close PostgreSQL database. Either the database was not open, or there was a problem closing it: " + e);
        }
    }
}
