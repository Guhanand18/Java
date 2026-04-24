package com.bnpparibas.ui;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

public class CsvExporter {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public static void export(JFrame parent, FilterPanel.HqlCriteria crit) {

        Session session = HibernateUtil.getSessionFactory().openSession();

        String hql = "from Employee e " + crit.where + " order by e.empId";
        Query<Employee> query = session.createQuery(hql, Employee.class);

        for (int i = 0; i < crit.params.size(); i++) {
            query.setParameter(i, crit.params.get(i));
        }

        List<Employee> rows = query.list();
        session.close();

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "No data matches the current filter - nothing to export.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV Export");
        chooser.setSelectedFile(new File("employees_export.csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File outFile = chooser.getSelectedFile();

        try (PrintWriter pw = new PrintWriter(outFile)) {

            // Header
            pw.println("Emp_ID,First Name,Last Name,Department,Position,Email,Phone,Address,DOB,Hire Date,Status,Created By,Import ID,File Name");

            for (Employee e : rows) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        esc(e.getEmpId()),
                        esc(e.getFirstName()),
                        esc(e.getLastName()),
                        esc(e.getDepartment()),
                        esc(e.getPosition()),
                        esc(e.getEmail()),
                        esc(e.getPhone()),
                        esc(e.getAddress()),
                        e.getDob() == null ? "" : DATE_FMT.format(e.getDob()),
                        e.getHireDate() == null ? "" : DATE_FMT.format(e.getHireDate()),
                        esc(e.getStatus()),
                        esc(e.getCreatedby()),
                        e.getImportId() == null ? "" : e.getImportId().toString(),
                        esc(e.getFilename())
                );
            }

            JOptionPane.showMessageDialog(parent,
                    "Export successful! " + rows.size() + " rows written to:\n" + outFile.getAbsolutePath(),
                    "Export", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Error while exporting CSV:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // CSV escape helper
    private static String esc(String v) {
        if (v == null) return "";
        String e = v.replace("\"", "\"\"");
        if (e.contains(",") || e.contains("\"") || e.contains("\n") || e.contains("\r")) {
            return "\"" + e + "\"";
        }
        return e;
    }
}