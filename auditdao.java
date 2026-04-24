package com.bnpparibas.dao;

import com.bnpparibas.model.*;
import com.bnpparibas.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditDAO {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public void writeAudit(Employee before, Employee after, String performedBy) {

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // 1. Get next version
            Audit latest = getLatestAuditForEmp(after.getEmpId());
            int nextVersion = (latest == null) ? 0 : latest.getVersionNumber() + 1;

            // 2. Save audit header
            Audit audit = new Audit(after.getEmpId(), performedBy, new Date(), nextVersion);
            Long auditId = (Long) session.save(audit);

            // 3. Compare fields and add details
            List<AuditDetail> details = new ArrayList<>();

            addIfChanged(details, auditId, "DEPARTMENT", before.getDepartment(), after.getDepartment());
            addIfChanged(details, auditId, "POSITION", before.getPosition(), after.getPosition());
            addIfChanged(details, auditId, "EMAIL", before.getEmail(), after.getEmail());
            addIfChanged(details, auditId, "PHONE", before.getPhone(), after.getPhone());
            addIfChanged(details, auditId, "ADDRESS", before.getAddress(), after.getAddress());

            String oldDob = before.getDob() != null ? DATE_FMT.format(before.getDob()) : "";
            String newDob = after.getDob() != null ? DATE_FMT.format(after.getDob()) : "";
            addIfChanged(details, auditId, "DOB", oldDob, newDob);

            // 4. Save details
            for (AuditDetail d : details) {
                session.save(d);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    // SIMPLE helper instead of lambda
    private void addIfChanged(List<AuditDetail> list, Long auditId,
                             String field, String oldVal, String newVal) {

        if (oldVal == null) oldVal = "";
        if (newVal == null) newVal = "";

        if (!oldVal.equals(newVal)) {
            list.add(new AuditDetail(auditId, field, oldVal, newVal));
        }
    }

    public List<Audit> getAuditsForEmp(String empId) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                    "FROM Audit WHERE empId = :e ORDER BY versionNumber DESC"
            ).setParameter("e", empId).list();
        } finally {
            session.close();
        }
    }

    public Audit getLatestAuditForEmp(String empId) {
        List<Audit> list = getAuditsForEmp(empId);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    public List<AuditDetail> getDetailsForAudit(Long auditId) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                    "FROM AuditDetail WHERE auditId = :a"
            ).setParameter("a", auditId).list();
        } finally {
            session.close();
        }
    }
}