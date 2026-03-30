package com.example.bank.dao;

import com.example.bank.models.TransactionRecord;
import com.example.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class TransactionDAO {

    public boolean addTransaction(TransactionRecord tr) {
        String sql = "INSERT INTO transactions (account_no, transaction_type, amount, note) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tr.getAccountNo());
            ps.setString(2, tr.getTransactionType());
            ps.setBigDecimal(3, tr.getAmount());
            ps.setString(4, tr.getNote());
            return ps.executeUpdate() == 1;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // deposit/withdraw/transfer with transaction management
    public boolean deposit(int accountNo, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        String select = "SELECT balance FROM accounts WHERE account_no=? FOR UPDATE";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(select)) {
                ps1.setInt(1, accountNo);
                ResultSet rs = ps1.executeQuery();
                if (!rs.next()) { conn.rollback(); return false; }
                BigDecimal bal = rs.getBigDecimal(1);
                BigDecimal newBal = bal.add(amount);
                // update account
                AccountDAO accountDAO = new AccountDAO();
                accountDAO.updateBalance(accountNo, newBal, conn);
                // insert transaction
                String insert = "INSERT INTO transactions (account_no, transaction_type, amount, note) VALUES (?,?,?,?)";
                try (PreparedStatement ps2 = conn.prepareStatement(insert)) {
                    ps2.setInt(1, accountNo);
                    ps2.setString(2, "Deposit");
                    ps2.setBigDecimal(3, amount);
                    ps2.setString(4, "Deposit via app");
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean withdraw(int accountNo, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        String select = "SELECT balance FROM accounts WHERE account_no=? FOR UPDATE";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(select)) {
                ps1.setInt(1, accountNo);
                ResultSet rs = ps1.executeQuery();
                if (!rs.next()) { conn.rollback(); return false; }
                BigDecimal bal = rs.getBigDecimal(1);
                if (bal.compareTo(amount) < 0) { conn.rollback(); return false; } // insufficient
                BigDecimal newBal = bal.subtract(amount);
                AccountDAO accountDAO = new AccountDAO();
                accountDAO.updateBalance(accountNo, newBal, conn);
                String insert = "INSERT INTO transactions (account_no, transaction_type, amount, note) VALUES (?,?,?,?)";
                try (PreparedStatement ps2 = conn.prepareStatement(insert)) {
                    ps2.setInt(1, accountNo);
                    ps2.setString(2, "Withdrawal");
                    ps2.setBigDecimal(3, amount);
                    ps2.setString(4, "Withdrawal via app");
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean transfer(int fromAcc, int toAcc, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // lock source
                String sel = "SELECT balance FROM accounts WHERE account_no=? FOR UPDATE";
                BigDecimal fromBal;
                try (PreparedStatement p = conn.prepareStatement(sel)) {
                    p.setInt(1, fromAcc);
                    ResultSet r = p.executeQuery();
                    if (!r.next()) { conn.rollback(); return false; }
                    fromBal = r.getBigDecimal(1);
                    if (fromBal.compareTo(amount) < 0) { conn.rollback(); return false; }
                }
                BigDecimal toBal;
                try (PreparedStatement p=conn.prepareStatement(sel)) {
                    p.setInt(1, toAcc);
                    ResultSet r = p.executeQuery();
                    if (!r.next()) { conn.rollback(); return false; }
                    toBal = r.getBigDecimal(1);
                }

                AccountDAO accountDAO = new AccountDAO();
                accountDAO.updateBalance(fromAcc, fromBal.subtract(amount), conn);
                accountDAO.updateBalance(toAcc, toBal.add(amount), conn);

                String ins = "INSERT INTO transactions (account_no, transaction_type, amount, note) VALUES (?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(ins)) {
                    ps.setInt(1, fromAcc);
                    ps.setString(2, "Transfer Out");
                    ps.setBigDecimal(3, amount);
                    ps.setString(4, "Transfer to " + toAcc);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(ins)) {
                    ps2.setInt(1, toAcc);
                    ps2.setString(2, "Transfer In");
                    ps2.setBigDecimal(3, amount);
                    ps2.setString(4, "Transfer from " + fromAcc);
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
