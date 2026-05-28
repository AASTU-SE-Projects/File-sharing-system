package main.java.database;

import main.java.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST =
            AppConfig.get("db.host");

    private static final String PORT =
            AppConfig.get("db.port");

    private static final String DB_NAME =
            AppConfig.get("db.name");

    private static final String USER =
            AppConfig.get("db.user");

    private static final String PASSWORD =
            AppConfig.get("db.password");

    public static Connection getServerConnection()
            throws SQLException {

        String url =
                "jdbc:mysql://" + HOST + ":" + PORT + "/";

        return DriverManager.getConnection(
                url,
                USER,
                PASSWORD
        );
    }

    public static Connection getConnection()
            throws SQLException {

        String url =
                "jdbc:mysql://" + HOST + ":" + PORT +
                        "/" + DB_NAME +
                        "?useSSL=false&allowPublicKeyRetrieval=true";

        return DriverManager.getConnection(
                url,
                USER,
                PASSWORD
        );
    }
}