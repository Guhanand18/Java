package com.bnpparibas.dao;

import java.util.List;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

public class EmployeeDAO {

    // ✅ CREATE
    public String createEmployee(Employee emp) {

        if (emp == null || emp.getEmpId() == null || emp.getEmpId().isEmpty()) {
            return null;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.save(emp);

            tx.commit();
            return emp.getEmpId();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Error saving employee: " + e.getMessage());
            return null;

        } finally {
            if (session != null) session.close();
        }
    }

    // ✅ UPDATE IMPORT INFO
    public void updateImportInfo(Employee emp) {

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.createQuery(
                    "UPDATE Employee SET importId = :impId, filename = :fn WHERE empId = :id")
                    .setParameter("impId", emp.getImportId())
                    .setParameter("fn", emp.getFilename())
                    .setParameter("id", emp.getEmpId())
                    .executeUpdate();

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Update error: " + e.getMessage());

        } finally {
            if (session != null) session.close();
        }
    }

    // ✅ GET ALL
    @SuppressWarnings("unchecked")
    public List<Employee> getAllEmployees() {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.createQuery("FROM Employee").list();

        } finally {
            if (session != null) session.close();
        }
    }

    // ✅ SEARCH
    @SuppressWarnings("unchecked")
    public List<Employee> searchEmployee(String keyword) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            return session.createQuery(
                    "FROM Employee WHERE firstName LIKE :k OR lastName LIKE :k OR empId LIKE :k")
                    .setParameter("k", "%" + keyword + "%")
                    .list();

        } finally {
            if (session != null) session.close();
        }
    }

    // ✅ DELETE
    public void deleteEmployee(String empId) {

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Employee emp = session.get(Employee.class, empId);

            if (emp != null) {
                session.delete(emp);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Delete error: " + e.getMessage());

        } finally {
            if (session != null) session.close();
        }
    }

    // ✅ DUPLICATE CHECK
    public boolean existsByDobAndPhone(Date dob, String phone) {

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Long count = (Long) session.createQuery(
                    "select count(e) from Employee e where e.dob = :d and e.phone = :p")
                    .setParameter("d", dob)
                    .setParameter("p", phone)
                    .uniqueResult();

            return count != null && count > 0;

        } finally {
            if (session != null) session.close();
        }
    }
}
