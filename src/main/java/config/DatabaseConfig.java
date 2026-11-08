package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:postgresql://localhost:5432/takenote_db";
    private static final String USER = "takenote_user";
    private static final String PASSWORD = "mypassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Имя базы данных из URL, для отображения в интерфейсе (например, "takenote_db"). */
    public static String databaseName() {
        String name = URL.substring(URL.lastIndexOf('/') + 1);
        int paramsIdx = name.indexOf('?');
        return paramsIdx == -1 ? name : name.substring(0, paramsIdx);
    }
}
