package com.bnpparibas.dao;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.List;

public class EmployeeDAO {

    public String createEmployee(Employee emp) {

        if (emp == null || emp.getEmpId() == null || emp.getEmpId().trim().isEmpty()) {
            return null;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // check duplicate
            Employee existing = (Employee) session.createQuery(
                    "FROM Employee WHERE empId = :eid")
                    .setParameter("eid", emp.getEmpId())
                    .uniqueResult();

            if (existing != null) {
                tx.rollback();
                return null;
            }

            session.save(emp);
            tx.commit();
            return emp.getEmpId();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return null;

        } finally {
            if (session != null) session.close();
        }
    }

    public void updateImportInfo(Employee emp) {

        if (emp == null) return;

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.createQuery(
                    "UPDATE Employee SET importId = :impId, filename = :fn WHERE empId = :eid"
            )
                    .setParameter("impId", emp.getImportId())
                    .setParameter("fn", emp.getFilename())
                    .setParameter("eid", emp.getEmpId())
                    .executeUpdate();

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();

        } finally {
            if (session != null) session.close();
        }
    }

    public List<Employee> listAll() {

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Employee").list();
        } finally {
            session.close();
        }
    }

    public boolean existsByDobAndPhone(Date dob, String phone) {

        if (dob == null || phone == null) return false;

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Long count = (Long) session.createQuery(
                    "SELECT COUNT(e) FROM Employee e WHERE e.dob = :d AND e.phone = :p"
            )
                    .setParameter("d", dob)
                    .setParameter("p", phone)
                    .uniqueResult();

            return count != null && count > 0;

        } finally {
            session.close();
        }
    }
}