package com.bnpparibas.dao;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;   // ✅ Hibernate 4.3
import org.hibernate.exception.ConstraintViolationException;

import java.util.Date;
import java.util.List;

public class EmployeeDAO {

    // ================= CREATE =================
    public String createEmployee(Employee emp) {

        if (emp == null || emp.getEmpId() == null || emp.getEmpId().trim().isEmpty()) {
            System.err.println("[EmployeeDAO] INSERT FAILED - empId missing");
            return null;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Check duplicate EMP_ID
            Query query = session.createQuery(
                    "FROM Employee WHERE empId = :eid"
            );

            query.setParameter("eid", emp.getEmpId());

            Employee existing = (Employee) query.uniqueResult();

            if (existing != null) {
                System.err.println("[EmployeeDAO] Duplicate EMP_ID (rejected): " + emp.getEmpId());
                tx.rollback();
                return null;
            }

            session.save(emp);
            tx.commit();

            return emp.getEmpId();

        } catch (ConstraintViolationException dup) {
            if (tx != null) tx.rollback();
            System.err.println("[EmployeeDAO] Constraint duplicate EMP_ID: " + emp.getEmpId());
            return null;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[EmployeeDAO] INSERT FAILED for empId=" + emp.getEmpId());
            e.printStackTrace();
            return null;

        } finally {
            if (session != null) session.close();
        }
    }

    // ================= UPDATE IMPORT INFO =================
    public void updateImportInfo(Employee emp) {

        if (emp == null || emp.getEmpId() == null) return;

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Query query = session.createQuery(
                    "UPDATE Employee SET importId = :impId, filename = :fn WHERE empId = :eid"
            );

            query.setParameter("impId", emp.getImportId());
            query.setParameter("fn", emp.getFilename());
            query.setParameter("eid", emp.getEmpId());

            query.executeUpdate();

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[EmployeeDAO] updateImportInfo FAILED for empId=" + emp.getEmpId());
            e.printStackTrace();

        } finally {
            if (session != null) session.close();
        }
    }

    // ================= LIST ALL =================
    public List<Employee> listAll() {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query query = session.createQuery("FROM Employee");

            List<Employee> list = query.list();

            return list;

        } finally {
            if (session != null) session.close();
        }
    }

    // ================= DUPLICATE CHECK =================
    public boolean existsByDobAndPhone(Date dob, String phone) {

        if (dob == null || phone == null) return false;

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query query = session.createQuery(
                    "select count(e) from Employee e where e.dob = :d and e.phone = :p"
            );

            query.setParameter("d", dob);
            query.setParameter("p", phone);

            Long count = (Long) query.uniqueResult();

            return count != null && count > 0;

        } finally {
            if (session != null) session.close();
        }
    }
}
