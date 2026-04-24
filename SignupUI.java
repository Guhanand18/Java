package com.bnpparibas.ui;

import javax.swing.*;

import com.bnpparibas.model.User;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SignupUI extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JCheckBox viewBox, editBox, validateBox;

    public SignupUI() {

        setTitle("Signup");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2, 10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        viewBox = new JCheckBox("VIEW");
        editBox = new JCheckBox("EDIT");
        validateBox = new JCheckBox("VALIDATE");

        JButton signupBtn = new JButton("Signup");

        add(new JLabel("Username:"));
        add(usernameField);

        add(new JLabel("Password:"));
        add(passwordField);

        add(viewBox);
        add(editBox);
        add(validateBox);
        add(new JLabel());

        add(signupBtn);

        signupBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                Session s = HibernateUtil.getSessionFactory().openSession();

                Query q = s.createQuery("FROM User WHERE username=:u");
                q.setParameter("u", username);

                List list = q.list();
                s.close();

                if (!list.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "User exists");
                    return;
                }

                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = session.beginTransaction();

                User u = new User();
                u.setUsername(username);
                u.setPassword(password);

                session.save(u);
                tx.commit();
                session.close();

                JOptionPane.showMessageDialog(null, "Signup success");
                new LoginUI();
                dispose();
            }
        });

        setVisible(true);
    }
}
