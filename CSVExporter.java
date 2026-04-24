package com.bnpparibas.ui;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Query;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

public class CsvExporter {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public static void export(JFrame parent, FilterPanel.HqlCriteria crit) {

        Session session = HibernateUtil.getSessionFactory().openSession();

        String hql = "FROM Employee e " + crit.where + " ORDER BY e.empId";

        Query query = session.createQuery(hql);

        for (int i = 0; i < crit.params.size(); i++) {
            query.setParameter(i + 1, crit.params.get(i)); // ✅ IMPORTANT (index starts from 1)
        }

        List<Employee> rows = query.list();
        session.close();

        if (rows == null || rows.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No data to export");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("employees.csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try {
            PrintWriter pw = new PrintWriter(file);

            // Header
            pw.println("Emp_ID,First Name,Last Name,Department,Position,Email,Phone,Address,DOB,Hire Date");

            for (Employee e : rows) {

                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        e.getEmpId(),
                        e.getFirstName(),
                        e.getLastName(),
                        e.getDepartment(),
                        e.getPosition(),
                        e.getEmail(),
                        e.getPhone(),
                        e.getAddress(),
                        e.getDob() == null ? "" : DATE_FMT.format(e.getDob()),
                        e.getHireDate() == null ? "" : DATE_FMT.format(e.getHireDate())
                );
            }

            pw.close();

            JOptionPane.showMessageDialog(parent,
                    "Export successful: " + rows.size() + " records");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Export failed: " + ex.getMessage());
        }
    }
}
