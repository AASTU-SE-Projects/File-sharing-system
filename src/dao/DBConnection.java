package dao;

import util.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect() {

        try {

            Connection conn = DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword());

            System.out.println("Database Connected");

            return conn;

            
        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
}