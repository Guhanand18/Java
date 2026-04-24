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

        String hql = "from Employee e " + crit.where + " order by e.empId";

        Query query = session.createQuery(hql);

        for (int i = 0; i < crit.params.size(); i++) {
            query.setParameter(i, crit.params.get(i));
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

        try {
            PrintWriter pw = new PrintWriter(chooser.getSelectedFile());

            pw.println("Emp_ID,First Name");

            for (Employee e : rows) {
                pw.println(e.getEmpId() + "," + e.getFirstName());
            }

            pw.close();

            JOptionPane.showMessageDialog(parent, "Export successful");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Export error");
        }
    }
}
