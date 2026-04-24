package com.bnpparibas.dao;

import com.bnpparibas.model.Audit;
import com.bnpparibas.model.AuditDetail;
import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditDAO {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * MAIN METHOD → writes audit
     */
    public void writeAudit(Employee before, Employee after, String performedBy) {

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // 1️⃣ Get next version
            int version = 0;
            Audit latest = getLatestAuditForEmp(after.getEmpId());

            if (latest != null) {
                version = latest.getVersionNumber() + 1;
            }

            // 2️⃣ Create audit header
            Audit audit = new Audit(
                    after.getEmpId(),
                    performedBy,
                    new Date(),
                    version
            );

            Long auditId = (Long) session.save(audit);

            // 3️⃣ Compare fields manually (simple logic)
            List<AuditDetail> details = new ArrayList<>();

            checkChange(details, auditId, "DEPARTMENT", before.getDepartment(), after.getDepartment());
            checkChange(details, auditId, "POSITION", before.getPosition(), after.getPosition());
            checkChange(details, auditId, "EMAIL", before.getEmail(), after.getEmail());
            checkChange(details, auditId, "PHONE", before.getPhone(), after.getPhone());
            checkChange(details, auditId, "ADDRESS", before.getAddress(), after.getAddress());

            // DOB special handling
            String oldDob = before.getDob() == null ? "" : DATE_FMT.format(before.getDob());
            String newDob = after.getDob() == null ? "" : DATE_FMT.format(after.getDob());
            checkChange(details, auditId, "DOB", oldDob, newDob);

            // 4️⃣ Save details
            for (AuditDetail d : details) {
                session.save(d);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[AuditDAO] Error writing audit");
            e.printStackTrace();

        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * SIMPLE helper → checks change
     */
    private void checkChange(List<AuditDetail> list, Long auditId,
                             String field, String oldVal, String newVal) {

        if (oldVal == null) oldVal = "";
        if (newVal == null) newVal = "";

        if (!oldVal.equals(newVal)) {
            list.add(new AuditDetail(auditId, field, oldVal, newVal));
        }
    }

    /**
     * Get all audits for employee
     */
    public List<Audit> getAuditsForEmp(String empId) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            return session.createQuery(
                    "FROM Audit WHERE empId = :e ORDER BY versionNumber DESC",
                    Audit.class
            ).setParameter("e", empId).list();

        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Get latest audit
     */
    public Audit getLatestAuditForEmp(String empId) {

        List<Audit> list = getAuditsForEmp(empId);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /**
     * Get audit details
     */
    public List<AuditDetail> getDetailsForAudit(Long auditId) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            return session.createQuery(
                    "FROM AuditDetail WHERE auditId = :a",
                    AuditDetail.class
            ).setParameter("a", auditId).list();

        } finally {
            if (session != null) session.close();
        }
    }
}