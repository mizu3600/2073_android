package com.example.clicker.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class DatabaseHelper {
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseHelper() {
    }

    public static void insertResponse(int questionNo, String choice) throws SQLException {
        String sql = "INSERT INTO responses (questionNo, choice) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionNo);
            statement.setString(2, choice);
            statement.executeUpdate();
        }
    }

    public static Map<String, Integer> countResponsesByChoice(int questionNo) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT choice, COUNT(*) AS total FROM responses WHERE questionNo = ? GROUP BY choice";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionNo);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String choice = resultSet.getString("choice");
                    int total = resultSet.getInt("total");
                    if (choice != null) {
                        counts.put(choice.toLowerCase(Locale.ENGLISH), total);
                    }
                }
            }
        }

        return counts;
    }

    public static void insertComment(int questionNo, String commentText) throws SQLException {
        String sql = "INSERT INTO comments (questionNo, commentText) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionNo);
            statement.setString(2, commentText);
            statement.executeUpdate();
        }
    }

    public static List<CommentEntry> listRecentComments(int questionNo, int limit) throws SQLException {
        List<CommentEntry> comments = new ArrayList<>();
        String sql = "SELECT commentText, createdAt FROM comments WHERE questionNo = ? ORDER BY createdAt DESC, id DESC LIMIT ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionNo);
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Timestamp createdAt = resultSet.getTimestamp("createdAt");
                    LocalDateTime createdDateTime = createdAt == null
                            ? LocalDateTime.now()
                            : createdAt.toLocalDateTime();
                    comments.add(new CommentEntry(
                            resultSet.getString("commentText"),
                            createdDateTime));
                }
            }
        }

        return comments;
    }

    private static Connection getConnection() throws SQLException {
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
