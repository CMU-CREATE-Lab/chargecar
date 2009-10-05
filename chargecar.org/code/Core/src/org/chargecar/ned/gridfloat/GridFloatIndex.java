package org.chargecar.ned.gridfloat;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GridFloatIndex
   {
   private static final Log LOG = LogFactory.getLog(GridFloatIndex.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(GridFloatIndex.class.getName());
   private static final String DATABASE_CONNECTION_URL = RESOURCES.getString("database.connection.url");
   private static final String DATABASE_CONNECTION_USERNAME = RESOURCES.getString("database.connection.username");
   private static final String DATABASE_CONNECTION_PASSWORD = RESOURCES.getString("database.connection.password");
   private static final String JDBC_DRIVER = RESOURCES.getString("database.jdbc.driver");

   static
      {
      try
         {
         Class.forName(JDBC_DRIVER);
         }
      catch (ClassNotFoundException e)
         {
         if (LOG.isErrorEnabled())
            {
            LOG.error("ClassNotFoundException while trying to load database driver [" + JDBC_DRIVER + "]", e);
            }
         }
      }

   private Connection connection;
   private PreparedStatement insertStatement;
   private PreparedStatement singleLocationSelectStatement;
   private PreparedStatement locationRangeSelectStatement;

   public GridFloatIndex()
      {
      }

   public boolean open()
      {
      try
         {
         connection = DriverManager.getConnection(DATABASE_CONNECTION_URL, DATABASE_CONNECTION_USERNAME, DATABASE_CONNECTION_PASSWORD);
         insertStatement = connection.prepareStatement("INSERT INTO grid_float_index VALUES (?,?,?,?,?,?,?,?,?,?,?)");
         singleLocationSelectStatement = connection.prepareStatement("SELECT header_file_path FROM grid_float_index WHERE ll_longitude <= ? AND ? <= ur_longitude AND ll_latitude <= ? AND ? <= ur_latitude ORDER BY cell_size");
         locationRangeSelectStatement = connection.prepareStatement("select header_file_path, ll_longitude, ll_latitude, ur_longitude, ur_latitude, cell_size from grid_float_index where ? <= ur_longitude and ll_longitude <= ? and ? <= ur_latitude and ll_latitude <= ? ORDER BY cell_size");
         return true;
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to open the database connection", e);
         }
      return false;
      }

   public boolean add(final GridFloatDataFile gridFloatDataFile)
      {
      try
         {
         insertStatement.setString(1, gridFloatDataFile.getHeaderFile().getAbsolutePath());
         insertStatement.setString(2, gridFloatDataFile.getDataFile().getAbsolutePath());
         insertStatement.setInt(3, gridFloatDataFile.getNumColumns());
         insertStatement.setInt(4, gridFloatDataFile.getNumRows());
         insertStatement.setDouble(5, gridFloatDataFile.getLowerLeftCornerLongitude());
         insertStatement.setDouble(6, gridFloatDataFile.getLowerLeftCornerLatitude());
         insertStatement.setDouble(7, gridFloatDataFile.getUpperRightCornerLongitude());
         insertStatement.setDouble(8, gridFloatDataFile.getUpperRightCornerLatitude());
         insertStatement.setDouble(9, gridFloatDataFile.getCellSize());
         insertStatement.setDouble(10, gridFloatDataFile.getNoDataValue());
         insertStatement.setBoolean(11, gridFloatDataFile.isBigEndian());
         if (insertStatement.executeUpdate() == 1)
            {
            return true;
            }
         else
            {
            if (LOG.isErrorEnabled())
               {
               LOG.error("Insert of GridFloatDataFile failed [" + gridFloatDataFile + "]");
               }
            }
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to execute or set fields in the INSERT prepared statement", e);
         }

      return false;
      }

   public List<GridFloatDataFile> lookup(final double longitude, final double latitude)
      {
      final List<GridFloatDataFile> dataFiles = new ArrayList<GridFloatDataFile>();

      try
         {
         singleLocationSelectStatement.setDouble(1, longitude);
         singleLocationSelectStatement.setDouble(2, longitude);
         singleLocationSelectStatement.setDouble(3, latitude);
         singleLocationSelectStatement.setDouble(4, latitude);
         lookup(dataFiles, singleLocationSelectStatement);
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to execute or set fields in the SELECT prepared statement", e);
         }

      return dataFiles;
      }

   public List<GridFloatDataFile> lookup(final double minLongitude, final double minLatitude, final double maxLongitude, final double maxLatitude)
      {
      final List<GridFloatDataFile> dataFiles = new ArrayList<GridFloatDataFile>();

      try
         {
         locationRangeSelectStatement.setDouble(1, minLongitude);
         locationRangeSelectStatement.setDouble(2, minLatitude);
         locationRangeSelectStatement.setDouble(3, maxLongitude);
         locationRangeSelectStatement.setDouble(4, maxLatitude);
         lookup(dataFiles, locationRangeSelectStatement);
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to execute or set fields in the SELECT prepared statement", e);
         }

      return dataFiles;
      }

   private void lookup(final List<GridFloatDataFile> dataFiles, final PreparedStatement statement) throws SQLException
      {
      final ResultSet resultSet = statement.executeQuery();
      if (resultSet != null)
         {
         while (resultSet.next())
            {
            final String headerFilePath = resultSet.getString("header_file_path");
            try
               {
               final GridFloatDataFile dataFile = GridFloatDataFile.create(new File(headerFilePath));
               if (dataFile != null)
                  {
                  dataFiles.add(dataFile);
                  }
               }
            catch (IOException e)
               {
               if (LOG.isErrorEnabled())
                  {
                  LOG.error("IOException while trying to create GridFloatDataFile for header file [" + headerFilePath + "]", e);
                  }
               }
            }
         }
      }

   public void close()
      {
      // close the INSERT statement
      try
         {
         if (insertStatement != null)
            {
            insertStatement.close();
            }
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to close the INSERT prepared statement", e);
         }

      // close the single location single location SELECT statement
      try
         {
         if (singleLocationSelectStatement != null)
            {
            singleLocationSelectStatement.close();
            }
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to close the single location SELECT prepared statement", e);
         }

      // close the single location location range SELECT statement
      try
         {
         if (locationRangeSelectStatement != null)
            {
            locationRangeSelectStatement.close();
            }
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to close the location range SELECT prepared statement", e);
         }

      // close the connection
      try
         {
         if (connection != null)
            {
            connection.close();
            }
         }
      catch (SQLException e)
         {
         LOG.error("SQLException while trying to close the database connection", e);
         }

      insertStatement = null;
      singleLocationSelectStatement = null;
      locationRangeSelectStatement = null;
      connection = null;
      }
   }
