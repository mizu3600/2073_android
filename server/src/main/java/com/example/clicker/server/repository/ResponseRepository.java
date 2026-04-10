package com.example.clicker.server.repository;

import com.example.clicker.server.config.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ResponseRepository {
    public void insertResponse(int questionNo, String choice) throws SQLException {
        String sql = "INSERT INTO responses (questionNo, choice) VALUES (?, ?)";

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionNo);
            statement.setString(2, choice);
            statement.executeUpdate();
        }
    }

    public Map<String, Integer> countResponsesByChoice(int questionNo) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT choice, COUNT(*) AS total FROM responses WHERE questionNo = ? GROUP BY choice";

        try (Connection connection = DatabaseConnectionFactory.getConnection();
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
}
