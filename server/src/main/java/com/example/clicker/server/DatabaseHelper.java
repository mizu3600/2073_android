package com.example.clicker.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseHelper {
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseHelper() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseHelper.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver is missing.", e);
        }

        return properties;
    }

    private static String getUrl() {
        return firstNonBlank(System.getenv("DB_URL"), PROPERTIES.getProperty("db.url"));
    }

    private static String getUser() {
        return firstNonBlank(System.getenv("DB_USER"), PROPERTIES.getProperty("db.user"));
    }

    private static String getPassword() {
        return firstNonBlank(System.getenv("DB_PASSWORD"), PROPERTIES.getProperty("db.password"));
    }

    private static String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return fallback == null ? "" : fallback;
    }
}
