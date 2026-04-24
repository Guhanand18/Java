package com.bnpparibas.dao;

import com.bnpparibas.model.Audit;
import com.bnpparibas.model.AuditDetail;
import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;   // ✅ Hibernate 4.3

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

            int version = 0;
            Audit latest = getLatestAuditForEmp(after.getEmpId());

            if (latest != null) {
                version = latest.getVersionNumber() + 1;
            }

            Audit audit = new Audit(
                    after.getEmpId(),
                    performedBy,
                    new Date(),
                    version
            );

            Long auditId = (Long) session.save(audit);

            List<AuditDetail> details = new ArrayList<AuditDetail>();

            checkChange(details, auditId, "DEPARTMENT", before.getDepartment(), after.getDepartment());
            checkChange(details, auditId, "POSITION", before.getPosition(), after.getPosition());
            checkChange(details, auditId, "EMAIL", before.getEmail(), after.getEmail());
            checkChange(details, auditId, "PHONE", before.getPhone(), after.getPhone());
            checkChange(details, auditId, "ADDRESS", before.getAddress(), after.getAddress());

            String oldDob = before.getDob() == null ? "" : DATE_FMT.format(before.getDob());
            String newDob = after.getDob() == null ? "" : DATE_FMT.format(after.getDob());

            checkChange(details, auditId, "DOB", oldDob, newDob);

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

    private void checkChange(List<AuditDetail> list, Long auditId,
                             String field, String oldVal, String newVal) {

        if (oldVal == null) oldVal = "";
        if (newVal == null) newVal = "";

        if (!oldVal.equals(newVal)) {
            list.add(new AuditDetail(auditId, field, oldVal, newVal));
        }
    }

    public List<Audit> getAuditsForEmp(String empId) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query query = session.createQuery(
                    "FROM Audit WHERE empId = :e ORDER BY versionNumber DESC"
            );

            query.setParameter("e", empId);

            List<Audit> list = query.list();

            return list;

        } finally {
            if (session != null) session.close();
        }
    }

    public Audit getLatestAuditForEmp(String empId) {

        List<Audit> list = getAuditsForEmp(empId);

        if (list == null || list.isEmpty()) return null;

        return list.get(0);
    }

    public List<AuditDetail> getDetailsForAudit(Long auditId) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query query = session.createQuery(
                    "FROM AuditDetail WHERE auditId = :a"
            );

            query.setParameter("a", auditId);

            List<AuditDetail> list = query.list();

            return list;

        } finally {
            if (session != null) session.close();
        }
    }
}
