package com.bnpparibas.dao;

import com.bnpparibas.model.ImportStats;
import com.bnpparibas.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ImportStatsDAO {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    public Long recordImport(String username, int valid, int invalid) {

        if (username == null) return null;

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            long id = System.currentTimeMillis() * 1000 + COUNTER.getAndIncrement();

            ImportStats stats = new ImportStats(username, 1, valid, invalid);
            stats.setId(id);

            session.save(stats);
            tx.commit();

            return id;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return null;

        } finally {
            if (session != null) session.close();
        }
    }

    public List<ImportStats> getAll() {

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM ImportStats").list();
        } finally {
            session.close();
        }
    }
}