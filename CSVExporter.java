package com.bnpparibas.ui;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Query;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class CsvExporter {

    public static void export(JFrame parent, FilterPanel.HqlCriteria crit) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Employee> rows;

        try {
            String hql = "from Employee e " + crit.where + " order by e.empId";
            Query query = session.createQuery(hql);

            for (int i = 0; i < crit.params.size(); i++) {
                query.setParameter(i, crit.params.get(i));
            }

            rows = query.list();

        } finally {
            session.close();
        }

        if (rows == null || rows.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No data to export");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("employees.csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try (PrintWriter pw = new PrintWriter(file)) {

            // Header
            pw.println("EmpID,First Name,Last Name,Department");

            // Data
            for (Employee e : rows) {
                pw.printf("%s,%s,%s,%s%n",
                        e.getEmpId(),
                        e.getFirstName(),
                        e.getLastName(),
                        e.getDepartment());
            }

            JOptionPane.showMessageDialog(parent, "Export successful");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Export failed");
        }
    }
}