package com.bnpparibas.ui;

import javax.swing.*;

import com.bnpparibas.model.User;
import com.bnpparibas.service.LoginService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginBtn, resetBtn, signupBtn;
    JCheckBox showPassword;

    public LoginUI() {

        setTitle("Login");
        setSize(400, 250);
        setLayout(new GridLayout(5, 2, 10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        showPassword = new JCheckBox("Show Password");
        add(showPassword);
        add(new JLabel(""));

        loginBtn = new JButton("Login");
        resetBtn = new JButton("Reset");
        signupBtn = new JButton("Signup");

        add(loginBtn);
        add(resetBtn);
        add(signupBtn);

        showPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '*');
            }
        });

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                LoginService service = new LoginService();
                User user = service.login(username, password);

                if (user == null) {
                    JOptionPane.showMessageDialog(LoginUI.this,
                            "Invalid credentials or already logged in");
                    return;
                }

                new EmployeeTableUI(user.getUsername()); // ✅ FIXED
                dispose();
            }
        });

        resetBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                usernameField.setText("");
                passwordField.setText("");
            }
        });

        signupBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SignupUI();
                dispose();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}
