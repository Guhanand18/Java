package com.bnpparibas.service;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.bnpparibas.model.User;
import com.bnpparibas.util.HibernateUtil;

public class LoginService {

    public User login(String username, String password) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query<User> query = session.createQuery(
                    "FROM User WHERE username = :u AND password = :p", User.class);

            query.setParameter("u", username);
            query.setParameter("p", password);

            User user = query.uniqueResult();

            if (user == null) return null;

            // check session
            if (!SessionManager.login(username)) {
                return null;
            }

            return user;

        } finally {
            if (session != null) session.close();
        }
    }
}