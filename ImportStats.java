package com.bnpparibas.dao;

import com.bnpparibas.model.ImportStats;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;   // ✅ Hibernate 4.3

import java.util.List;

public class ImportStatsDAO {

    public Long recordImport(String username, int valid, int invalid) {

        if (username == null || username.trim().isEmpty()) {
            System.err.println("[ImportStatsDAO] Username is missing");
            return null;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            ImportStats stats = new ImportStats();
            stats.setUsername(username);
            stats.setFilesImported(1);
            stats.setTotalValid(valid);
            stats.setTotalInvalid(invalid);

            session.save(stats);

            tx.commit();

            return stats.getId();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[ImportStatsDAO] Error while saving import stats");
            e.printStackTrace();
            return null;

        } finally {
            if (session != null) session.close();
        }
    }

    public void printAllImports() {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query query = session.createQuery("FROM ImportStats");

            List<ImportStats> list = query.list();

            System.out.println("\n===== IMPORT STATS =====");

            for (ImportStats s : list) {
                System.out.printf(
                        "%d | %s | %d | %d | %d%n",
                        s.getId(),
                        s.getUsername(),
                        s.getFilesImported(),
                        s.getTotalValid(),
                        s.getTotalInvalid()
                );
            }

            System.out.println("===== END =====\n");

        } catch (Exception e) {
            System.err.println("[ImportStatsDAO] Error fetching stats");
            e.printStackTrace();

        } finally {
            if (session != null) session.close();
        }
    }
}
