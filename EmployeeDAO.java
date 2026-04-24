package com.bnpparibas.dao;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
            Employee existing = session.createQuery(
                    "FROM Employee WHERE empId = :eid", Employee.class)
                    .setParameter("eid", emp.getEmpId())
                    .uniqueResult();

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

            session.createQuery(
                    "UPDATE Employee SET importId = :impId, filename = :fn WHERE empId = :eid")
                    .setParameter("impId", emp.getImportId())
                    .setParameter("fn", emp.getFilename())
                    .setParameter("eid", emp.getEmpId())
                    .executeUpdate();

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

            Query<Employee> query = session.createQuery("FROM Employee", Employee.class);
            return query.list();

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

            Long count = session.createQuery(
                    "select count(e) from Employee e where e.dob = :d and e.phone = :p",
                    Long.class)
                    .setParameter("d", dob)
                    .setParameter("p", phone)
                    .uniqueResult();

            return count != null && count > 0;

        } finally {
            if (session != null) session.close();
        }
    }
}