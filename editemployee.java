package com.bnpparibas.ui;

import javax.swing.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.bnpparibas.dao.AuditDAO;
import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditEmployeeUI extends JFrame {

    private JTextField emailField, phoneField, addressField, hireDateField;
    private JComboBox<String> deptCombo, posCombo;

    private final String empId;
    private final EmployeeTableUI parent;
    private final String currentUser;

    private final AuditDAO auditDAO = new AuditDAO();

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public EditEmployeeUI(String empId, EmployeeTableUI parent, String currentUser) {

        this.empId = empId;
        this.parent = parent;
        this.currentUser = currentUser;

        setTitle("Edit Employee");
        setSize(400, 400);
        setLayout(null);
        setLocationRelativeTo(null);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Employee emp = (Employee) session.get(Employee.class, empId);
        session.close();

        int y = 30;

        deptCombo = new JComboBox<>(new String[]{"Finance","HR","IT","Marketing"});
        deptCombo.setSelectedItem(emp.getDepartment());
        deptCombo.setBounds(120, y, 200, 25); add(deptCombo); y+=40;

        posCombo = new JComboBox<>(new String[]{"Analyst","Developer","Manager"});
        posCombo.setSelectedItem(emp.getPosition());
        posCombo.setBounds(120, y, 200, 25); add(posCombo); y+=40;

        emailField = new JTextField(emp.getEmail());
        emailField.setBounds(120, y, 200, 25); add(emailField); y+=40;

        phoneField = new JTextField(emp.getPhone());
        phoneField.setBounds(120, y, 200, 25); add(phoneField); y+=40;

        addressField = new JTextField(emp.getAddress());
        addressField.setBounds(120, y, 200, 25); add(addressField); y+=40;

        hireDateField = new JTextField(
                emp.getHireDate() == null ? "" : DATE_FMT.format(emp.getHireDate()));
        hireDateField.setBounds(120, y, 200, 25); add(hireDateField); y+=50;

        JButton save = new JButton("Save");
        save.setBounds(150, y, 100, 30);
        add(save);

        save.addActionListener(e -> save(emp));

        setVisible(true);
    }

    private void save(Employee oldEmp) {

        if (!validateForm()) return;

        Employee before = cloneEmployee(oldEmp);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            Employee emp = (Employee) session.get(Employee.class, empId);

            emp.setDepartment((String) deptCombo.getSelectedItem());
            emp.setPosition((String) posCombo.getSelectedItem());
            emp.setEmail(emailField.getText());
            emp.setPhone(phoneField.getText());
            emp.setAddress(addressField.getText());
            emp.setHireDate(parseDate(hireDateField.getText()));

            emp.setStatus("TO_BE_VALIDATED");
            emp.setLastModifiedBy(currentUser);

            session.update(emp);
            tx.commit();

            auditDAO.writeAudit(before, emp, currentUser);

            JOptionPane.showMessageDialog(this, "Updated");

            parent.refreshAfterEdit();
            dispose();

        } catch (Exception e) {
            tx.rollback();
        } finally {
            session.close();
        }
    }

    private boolean validateForm() {

        if (!emailField.getText().matches("^.+@.+\\..+$")) {
            JOptionPane.showMessageDialog(this, "Invalid Email");
            return false;
        }

        if (!phoneField.getText().matches("[6-9]\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Invalid Phone");
            return false;
        }

        return true;
    }

    private Date parseDate(String s) {
        try {
            return DATE_FMT.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Employee cloneEmployee(Employee src) {

        Employee e = new Employee();
        e.setEmpId(src.getEmpId());
        e.setDepartment(src.getDepartment());
        e.setPosition(src.getPosition());
        e.setEmail(src.getEmail());
        e.setPhone(src.getPhone());
        e.setAddress(src.getAddress());
        e.setDob(src.getDob());
        e.setHireDate(src.getHireDate());

        return e;
    }
}