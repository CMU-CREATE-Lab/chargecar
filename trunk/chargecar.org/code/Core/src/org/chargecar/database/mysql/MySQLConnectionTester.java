package org.chargecar.database.mysql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <p>
 * <code>MySQLConnectionTester</code> helps test MySQL database connections from the command line.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class MySQLConnectionTester
   {
   private static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";

   public static void main(final String[] args) throws IOException, ClassNotFoundException, SQLException
      {
      final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      System.out.println("Loading MySQL JDBC driver (" + MYSQL_JDBC_DRIVER + ")...");
      Class.forName(MYSQL_JDBC_DRIVER);
      System.out.println("Driver loaded successfully!");

      System.out.println();
      System.out.print("Database connection url: ");
      final String connectionURL = in.readLine();

      System.out.print("Database username: ");
      final String username = in.readLine();

      System.out.print("Database password: ");
      final String password = in.readLine();

      System.out.println();
      System.out.println("Establishing connection...");
      final Connection connection = DriverManager.getConnection(connectionURL, username, password);
      if (connection != null)
         {
         System.out.println("Connection successful!");
         connection.close();
         }
      else
         {
         System.out.println("Connection failed.");
         }
      }

   private MySQLConnectionTester()
      {
      // private to prevent instantiation
      }
   }
