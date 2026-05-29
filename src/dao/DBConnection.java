package dao;

import util.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    // Establishes and returns a connection to the MySQL database
    public static Connection connect() {

        try {

            // Create database connection using configuration values from AppConfig
            Connection conn = DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword());

            // Confirm successful database connection
            System.out.println("Database Connected");

            // Return active connection object
            return conn;

        } catch (Exception e) {

            // Print error details if connection fails
            e.printStackTrace();
        }

        // Return null if connection was not successful
        return null;
    }
}