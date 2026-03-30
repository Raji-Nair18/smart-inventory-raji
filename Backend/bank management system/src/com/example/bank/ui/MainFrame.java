package com.example.bank.ui;

import com.example.bank.dao.AccountDAO;
import com.example.bank.dao.CustomerDAO;
import com.example.bank.dao.TransactionDAO;
import com.example.bank.models.Account;
import com.example.bank.models.Customer;
import com.example.bank.models.TransactionRecord;
import com.example.bank.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class MainFrame extends JFrame {
    private User currentUser;
    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    private DefaultTableModel customerTableModel;

    public MainFrame(User u) {
        this.currentUser = u;
        setTitle("Bank Management - " + u.getRole() + " : " + u.getUsername());
        setSize(900,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();

        // Customers tab
        JPanel pCustomers = new JPanel(new BorderLayout());
        customerTableModel = new DefaultTableModel(new String[]{"ID","Name","Phone","Email","Type"},0);
        JTable tblCustomers = new JTable(customerTableModel);
        refreshCustomers();
        pCustomers.add(new JScrollPane(tblCustomers), BorderLayout.CENTER);

        JPanel addCust = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField name = new JTextField(10);
        JTextField phone = new JTextField(8);
        JTextField email = new JTextField(10);
        JComboBox<String> acctType = new JComboBox<>(new String[]{"Savings","Current"});
        JButton btnAdd = new JButton("Add Customer");
        addCust.add(new JLabel("Name")); addCust.add(name);
        addCust.add(new JLabel("Phone")); addCust.add(phone);
        addCust.add(new JLabel("Email")); addCust.add(email);
        addCust.add(new JLabel("Type")); addCust.add(acctType);
        addCust.add(btnAdd);
        pCustomers.add(addCust, BorderLayout.NORTH);

        btnAdd.addActionListener(e -> {
            Customer c = new Customer();
            c.setName(name.getText().trim());
            c.setPhone(phone.getText().trim());
            c.setEmail(email.getText().trim());
            c.setAccountType((String)acctType.getSelectedItem());
            c.setDateOfBirth(new Date(System.currentTimeMillis())); // demo DOB = today
            int id = customerDAO.addCustomer(c);
            if (id>0) {
                JOptionPane.showMessageDialog(this,"Customer added with ID: "+id);
                refreshCustomers();
            } else JOptionPane.showMessageDialog(this,"Failed to add");
        });

        tabs.addTab("Customers", pCustomers);

        // Accounts & Transactions tab
        JPanel pAccounts = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtCustId = new JTextField(5);
        JTextField txtInitial = new JTextField(8);
        JButton btnCreateAcc = new JButton("Create Account");
        top.add(new JLabel("Customer ID")); top.add(txtCustId);
        top.add(new JLabel("Initial Deposit")); top.add(txtInitial);
        top.add(btnCreateAcc);
        pAccounts.add(top, BorderLayout.NORTH);

        btnCreateAcc.addActionListener(e -> {
            try {
                int cid = Integer.parseInt(txtCustId.getText().trim());
                BigDecimal dep = new BigDecimal(txtInitial.getText().trim());
                int accNo = accountDAO.createAccount(cid, dep);
                if (accNo>0) JOptionPane.showMessageDialog(this,"Account created: "+accNo);
                else JOptionPane.showMessageDialog(this,"Failed to create account");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input"); }
        });

        // Deposit/Withdraw/Transfer UI
        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tAcc = new JTextField(6), tAmt = new JTextField(8), tToAcc = new JTextField(6);
        JButton btnDep = new JButton("Deposit"), btnWith = new JButton("Withdraw"), btnTrans = new JButton("Transfer");
        ops.add(new JLabel("Account")); ops.add(tAcc);
        ops.add(new JLabel("Amount")); ops.add(tAmt);
        ops.add(btnDep); ops.add(btnWith);
        ops.add(new JLabel("To Acc")); ops.add(tToAcc); ops.add(btnTrans);
        pAccounts.add(ops, BorderLayout.CENTER);

        btnDep.addActionListener(e -> {
            try {
                int acc = Integer.parseInt(tAcc.getText().trim());
                BigDecimal amt = new BigDecimal(tAmt.getText().trim());
                boolean ok = transactionDAO.deposit(acc,amt);
                JOptionPane.showMessageDialog(this, ok ? "Deposit success" : "Deposit failed");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input"); }
        });

        btnWith.addActionListener(e -> {
            try {
                int acc = Integer.parseInt(tAcc.getText().trim());
                BigDecimal amt = new BigDecimal(tAmt.getText().trim());
                boolean ok = transactionDAO.withdraw(acc,amt);
                JOptionPane.showMessageDialog(this, ok ? "Withdrawal success" : "Withdrawal failed (insufficient?)");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input"); }
        });

        btnTrans.addActionListener(e -> {
            try {
                int acc = Integer.parseInt(tAcc.getText().trim());
                int to = Integer.parseInt(tToAcc.getText().trim());
                BigDecimal amt = new BigDecimal(tAmt.getText().trim());
                boolean ok = transactionDAO.transfer(acc,to,amt);
                JOptionPane.showMessageDialog(this, ok ? "Transfer success" : "Transfer failed");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input"); }
        });

        tabs.addTab("Accounts & Transactions", pAccounts);

        // Transactions history tab (simple)
        JPanel pTrans = new JPanel(new BorderLayout());
        JButton btnRefreshTrans = new JButton("Refresh Last 100 Transactions");
        JTextArea txtTrans = new JTextArea();
        txtTrans.setEditable(false);
        pTrans.add(btnRefreshTrans, BorderLayout.NORTH);
        pTrans.add(new JScrollPane(txtTrans), BorderLayout.CENTER);

        btnRefreshTrans.addActionListener(e -> {
            // quick simple list
            StringBuilder sb = new StringBuilder();
            try (var conn = com.example.bank.util.DBConnection.getConnection();
                 var ps = conn.prepareStatement("SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT 100");
                 var rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append(String.format("ID:%d Acc:%d %s %.2f %s\n",
                        rs.getInt("transaction_id"),
                        rs.getInt("account_no"),
                        rs.getString("transaction_type"),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("transaction_date").toString()
                    ));
                }
            } catch (Exception ex) { 
                ex.printStackTrace(); sb.append("Error loading"); }
            txtTrans.setText(sb.toString());
        });

        tabs.addTab("Transactions", pTrans);

        add(tabs);
    }

    private void refreshCustomers() {
        customerTableModel.setRowCount(0);
        List<Customer> list = customerDAO.getAllCustomers();
        for (Customer c : list) {
            customerTableModel.addRow(new Object[]{
                    c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail(), c.getAccountType()
            });
        }
    }
}