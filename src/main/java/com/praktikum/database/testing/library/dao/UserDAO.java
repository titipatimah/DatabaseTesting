package com.praktikum.database.testing.library.dao;

import com.praktikum.database.testing.library.config.DatabaseConfig;
import com.praktikum.database.testing.library.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    // CREATE: Insert user baru ke database
    public User create(User user) throws SQLException {

        String sql = "INSERT INTO users (username, email, full_name, phone, role, status) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "RETURNING user_id, registration_date, created_at, updated_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getRole() != null ? user.getRole() : "member");
            pstmt.setString(6, user.getStatus() != null ? user.getStatus() : "active");

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user.setUserId(rs.getInt("user_id"));
                user.setRegistrationDate(rs.getTimestamp("registration_date"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                return user;
            }
        }

        return null;
    }

    // READ: find by id
    public Optional<User> findById(Integer userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        }
    }

    // READ: find by username
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        }
    }

    // READ: find by email
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        }
    }

    // READ: find all
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    // SEARCH: search by name (full_name) using ILIKE for case-insensitive (Postgres) or LIKE for others
    public List<User> searchByName(String keyword) throws SQLException {
        String sql = "SELECT * FROM users WHERE full_name ILIKE ? ORDER BY user_id"; // ILIKE for Postgres
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    // UPDATE
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, full_name = ?, phone = ?, "
                + "role = ?, status = ?, last_login = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getStatus());
            pstmt.setTimestamp(6, user.getLastLogin());
            pstmt.setInt(7, user.getUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(Integer userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Helper mapping
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .userId(rs.getInt("user_id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .fullName(rs.getString("full_name"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .status(rs.getString("status"))
                .registrationDate(rs.getTimestamp("registration_date"))
                .lastLogin(rs.getTimestamp("last_login"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    // COUNT
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    // UPDATE last login
    public boolean updateLastLogin(Integer userId) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
