package com.bnpparibas.ui;

import com.bnpparibas.model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class TablePanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd");

    public TablePanel() {

        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F5F5));

        String[] columns = {
                "Emp ID", "First Name", "Last Name",
                "Department", "Position", "Email", "Phone",
                "Address", "DOB", "Hire Date",
                "Status", "Created By", "Import ID", "File Name"
        };

        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void refresh(List<Employee> employees) {

        model.setRowCount(0);

        for (Employee e : employees) {

            model.addRow(new Object[]{
                    e.getEmpId(),
                    e.getFirstName(),
                    e.getLastName(),
                    e.getDepartment(),
                    e.getPosition(),
                    e.getEmail(),
                    e.getPhone(),
                    e.getAddress(),
                    formatDate(e.getDob()),
                    formatDate(e.getHireDate()),
                    e.getStatus(),
                    e.getCreatedBy(),
                    e.getImportId(),
                    e.getFilename()
            });
        }
    }

    private String formatDate(java.util.Date d) {
        return d == null ? "" : DATE_FMT.format(d);
    }

    public JTable getTable() {
        return table;
    }
}