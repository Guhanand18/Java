package com.bnpparibas.service;

import org.hibernate.Session;
import org.hibernate.Query;   // ✅ NOTE: old import

import com.bnpparibas.model.User;
import com.bnpparibas.util.HibernateUtil;
import com.bnpparibas.util.SessionManager;

public class LoginService {

    public User login(String username, String password) {

        Session session = HibernateUtil.getSessionFactory().openSession();

        Query query = session.createQuery(
                "FROM User WHERE username = :u AND password = :p"
        );

        query.setParameter("u", username);
        query.setParameter("p", password);

        User user = (User) query.uniqueResult();  // ✅ casting required

        session.close();

        if (user == null) {
            return null;
        }

        if (!SessionManager.login(username)) {
            return null;
        }

        return user;
    }
}
