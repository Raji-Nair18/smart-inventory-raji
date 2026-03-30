package com.example.bank.ui;

import com.example.bank.dao.UserDAO;
import com.example.bank.models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginFrame() {
        setTitle("Bank Management - Login");
        setSize(350,200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        init();
    }

    private void init() {
        JPanel p = new JPanel(new GridLayout(3,2,10,10));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        p.add(new JLabel("Username:"));
        txtUser = new JTextField();
        p.add(txtUser);
        p.add(new JLabel("Password:"));
        txtPass = new JPasswordField();
        p.add(txtPass);
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(this::loginAction);
        p.add(new JLabel());
        p.add(btnLogin);
        add(p);
    }

    private void loginAction(ActionEvent e) {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Enter credentials","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        UserDAO dao = new UserDAO();
        User user = dao.authenticate(u,p);
        if (user != null) {
            SwingUtilities.invokeLater(() -> {
                MainFrame main = new MainFrame(user);
                main.setVisible(true);
            });
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,"Invalid credentials","Login Failed",JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> new LoginFrame().setVisible(true));
    }
}
