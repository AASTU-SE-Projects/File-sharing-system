package main.java.database;

import main.java.config.AppConfig;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        String dbName = AppConfig.get("db.name");

        try {

            // CREATE DATABASE
            try (Connection conn =
                         DBConnection.getServerConnection();

                 Statement stmt =
                         conn.createStatement()) {

                stmt.executeUpdate(
                        "CREATE DATABASE IF NOT EXISTS " + dbName
                );

                System.out.println("Database checked/created.");
            }

            // CREATE TABLES
            try (Connection conn =
                         DBConnection.getConnection();

                 Statement stmt =
                         conn.createStatement()) {

                // USERS TABLE
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        
                        username VARCHAR(100) NOT NULL,
                        
                        email VARCHAR(100) UNIQUE NOT NULL,
                        
                        password_hash VARCHAR(255) NOT NULL,
                        
                        created_at TIMESTAMP
                        DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                // FILES TABLE
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS files (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        
                        original_filename VARCHAR(255)
                        NOT NULL,
                        
                        stored_filename VARCHAR(255)
                        NOT NULL UNIQUE,
                        
                        filesize BIGINT NOT NULL,
                        
                        uploaded_by INT NOT NULL,
                        
                        upload_date TIMESTAMP
                        DEFAULT CURRENT_TIMESTAMP,
                        
                        share_token VARCHAR(100) UNIQUE,
                        
                        FOREIGN KEY (uploaded_by)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                    )
                """);

                System.out.println("Tables checked/created.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}