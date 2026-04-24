package com.bnpparibas.ui;

import javax.swing.*;

import com.bnpparibas.model.User;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;   // ✅ Hibernate 4.3 import

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SignupUI extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;

    JCheckBox viewBox, editBox, validateBox;
    JCheckBox showPassword;

    JButton signupBtn, backBtn;

    public SignupUI() {

        setTitle("Signup");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2, 10, 10));
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

        add(new JLabel("Roles:"));
        JPanel rolePanel = new JPanel();

        viewBox = new JCheckBox("VIEW");
        editBox = new JCheckBox("EDIT");
        validateBox = new JCheckBox("VALIDATE");

        rolePanel.add(viewBox);
        rolePanel.add(editBox);
        rolePanel.add(validateBox);

        add(rolePanel);

        signupBtn = new JButton("Signup");
        backBtn = new JButton("Back");

        add(signupBtn);
        add(backBtn);

        // Show password
        showPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showPassword.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('*');
                }
            }
        });

        // Back
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LoginUI();
                dispose();
            }
        });

        // Signup
        signupBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(SignupUI.this,
                            "All fields are required");
                    return;
                }

                if (password.length() < 6) {
                    JOptionPane.showMessageDialog(SignupUI.this,
                            "Password must be at least 6 characters");
                    return;
                }

                String role = "";

                if (viewBox.isSelected()) role += "VIEW,";
                if (editBox.isSelected()) role += "EDIT,";
                if (validateBox.isSelected()) role += "VALIDATE,";

                if (role.isEmpty()) {
                    JOptionPane.showMessageDialog(SignupUI.this,
                            "Select at least one role");
                    return;
                }

                role = role.substring(0, role.length() - 1);

                // ================= DUPLICATE CHECK =================
                Session checkSession = HibernateUtil.getSessionFactory().openSession();

                Query query = checkSession.createQuery(
                        "FROM User WHERE username = :u"
                );

                query.setParameter("u", username);

                List list = query.list();
                checkSession.close();

                if (!list.isEmpty()) {
                    JOptionPane.showMessageDialog(SignupUI.this,
                            "Username already exists");
                    return;
                }

                // ================= SAVE USER =================
                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = session.beginTransaction();

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(role);

                session.save(user);
                tx.commit();
                session.close();

                JOptionPane.showMessageDialog(SignupUI.this,
                        "User registered successfully");

                new LoginUI();
                dispose();
            }
        });

        setVisible(true);
    }
}
