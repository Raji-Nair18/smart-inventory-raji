package com.example.bank.dao;

import com.example.bank.models.User;
import com.example.bank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password, role FROM users WHERE username=? AND password=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // NOTE: plain text for demo
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                 User u = new User();
                  u.setUserId(rs.getInt("user_id"));
                  u.setUsername(rs.getString("username"));
                  u.setPassword(rs.getString("password"));
                  u.setRole(rs.getString("role"));
            return u;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}