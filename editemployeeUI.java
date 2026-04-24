package com.bnpparibas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.StaleObjectStateException;

import com.bnpparibas.dao.AuditDAO;
import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

public class EditEmployeeUI extends JFrame {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> deptCombo;
    private JComboBox<String> positionCombo;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField hireDateField;
    private JTextField dobField;

    private JButton saveBtn = new JButton("Save");

    private String empId;
    private EmployeeTableUI parent;
    private String currentUser;

    private AuditDAO auditDAO = new AuditDAO();

    private Employee originalEmployee;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    static {
        DATE_FMT.setLenient(false);
    }

    private static final String[] DEPARTMENTS = {"Finance", "HR", "IT", "Marketing"};
    private static final String[] POSITIONS = {"Analyst", "Recruiter", "Developer", "Manager"};

    public EditEmployeeUI(String empId, EmployeeTableUI parent, String currentUser) {

        this.empId = empId;
        this.parent = parent;
        this.currentUser = currentUser;

        setTitle("Edit Employee");
        setSize(460, 580);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // LOAD EMPLOYEE
        Session session = HibernateUtil.getSessionFactory().openSession();
        Employee emp = (Employee) session.get(Employee.class, empId);
        session.close();

        if (emp == null) {
            JOptionPane.showMessageDialog(this,
                    "Employee not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        originalEmployee = emp;

        // UI
        int left = 30, fieldX = 150, width = 250, height = 25, y = 30, gap = 40;

        addLabel("First Name:", left, y);
        firstNameField = new JTextField(emp.getFirstName());
        setField(firstNameField, fieldX, y, width, height, false);
        y += gap;

        addLabel("Last Name:", left, y);
        lastNameField = new JTextField(emp.getLastName());
        setField(lastNameField, fieldX, y, width, height, false);
        y += gap;

        addLabel("Department:", left, y);
        deptCombo = new JComboBox<>(DEPARTMENTS);
        deptCombo.setSelectedItem(emp.getDepartment());
        setComponent(deptCombo, fieldX, y, width, height);
        y += gap;

        addLabel("Position:", left, y);
        positionCombo = new JComboBox<>(POSITIONS);
        positionCombo.setSelectedItem(emp.getPosition());
        setComponent(positionCombo, fieldX, y, width, height);
        y += gap;

        addLabel("Email:", left, y);
        emailField = new JTextField(emp.getEmail());
        setField(emailField, fieldX, y, width, height, true);
        y += gap;

        addLabel("Phone:", left, y);
        phoneField = new JTextField(emp.getPhone());
        setField(phoneField, fieldX, y, width, height, true);
        y += gap;

        addLabel("Address:", left, y);
        addressField = new JTextField(emp.getAddress());
        setField(addressField, fieldX, y, width, height, true);
        y += gap;

        addLabel("Hire Date:", left, y);
        String hireDate = emp.getHireDate() == null ? "" : DATE_FMT.format(emp.getHireDate());
        hireDateField = new JTextField(hireDate);
        setField(hireDateField, fieldX, y, width, height, true);
        y += gap;

        addLabel("DOB:", left, y);
        String dob = emp.getDob() == null ? "" : DATE_FMT.format(emp.getDob());
        dobField = new JTextField(dob);
        setField(dobField, fieldX, y, width, height, false);
        y += gap;

        saveBtn.setBounds(180, y, 100, 30);
        add(saveBtn);

        // BUTTON ACTION (no lambda)
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (!validateForm()) return;

                Employee before = cloneEmployee(empId);

                Session sess = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = sess.beginTransaction();

                Employee managed = (Employee) sess.get(Employee.class, empId);

                managed.setDepartment((String) deptCombo.getSelectedItem());
                managed.setPosition((String) positionCombo.getSelectedItem());
                managed.setEmail(emailField.getText().trim());
                managed.setPhone(phoneField.getText().trim());
                managed.setAddress(addressField.getText().trim());

                try {
                    Date parsed = parseHireDate();
                    managed.setHireDate(parsed);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EditEmployeeUI.this,
                            "Invalid Hire Date",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                managed.setLastModifiedBy(currentUser);

                try {
                    sess.update(managed);
                    tx.commit();
                } catch (StaleObjectStateException ex) {
                    tx.rollback();
                    JOptionPane.showMessageDialog(EditEmployeeUI.this,
                            "Record modified by another user",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                auditDAO.writeAudit(before, managed, currentUser);

                JOptionPane.showMessageDialog(EditEmployeeUI.this,
                        "Employee Updated",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                parent.refreshAfterEdit();
                dispose();
            }
        });

        setVisible(true);
    }

    private void addLabel(String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setBounds(x, y, 100, 25);
        add(l);
    }

    private void setField(JTextField f, int x, int y, int w, int h, boolean editable) {
        f.setBounds(x, y, w, h);
        f.setEditable(editable);
        add(f);
    }

    private void setComponent(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        add(c);
    }

    private boolean validateForm() {

        String phone = phoneField.getText().trim();
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 digits");
            return false;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Invalid Email");
            return false;
        }

        return true;
    }

    private Date parseHireDate() throws ParseException {
        String txt = hireDateField.getText().trim();
        if (txt.isEmpty()) return null;
        return DATE_FMT.parse(txt);
    }

    private Employee cloneEmployee(String id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        Employee src = (Employee) s.get(Employee.class, id);
        s.close();

        Employee copy = new Employee();
        copy.setEmpId(src.getEmpId());
        copy.setFirstName(src.getFirstName());
        copy.setLastName(src.getLastName());
        copy.setDepartment(src.getDepartment());
        copy.setPosition(src.getPosition());
        copy.setEmail(src.getEmail());
        copy.setPhone(src.getPhone());
        copy.setAddress(src.getAddress());
        copy.setDob(src.getDob());
        copy.setHireDate(src.getHireDate());

        return copy;
    }
}