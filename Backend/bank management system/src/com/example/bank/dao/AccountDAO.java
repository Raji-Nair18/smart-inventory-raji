package com.example.bank.dao;

import com.example.bank.models.Account;
import com.example.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class AccountDAO {

    public int createAccount(int customerId, BigDecimal initialDeposit) {
        String sql = "INSERT INTO accounts (customer_id, balance, opening_date, status) VALUES (?,?,CURRENT_DATE,'Active')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setBigDecimal(2, initialDeposit);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public Account getAccount(int accountNo) {
        String sql = "SELECT * FROM accounts WHERE account_no=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Account a = new Account();
                a.setAccountNo(rs.getInt("account_no"));
                a.setCustomerId(rs.getInt("customer_id"));
                a.setBalance(rs.getBigDecimal("balance"));
                a.setOpeningDate(rs.getDate("opening_date"));
                a.setStatus(rs.getString("status"));
                return a;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateBalance(int accountNo, BigDecimal newBalance, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance=? WHERE account_no=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountNo);
            return ps.executeUpdate() == 1;
        }
    }
}
