package com.bnpparibas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter panel with all fields (unchanged functionality).
 * Builds HQL WHERE clause dynamically.
 */
public class FilterPanel extends JPanel {

    // Text fields
    private final JTextField tfEmpId = new JTextField(12);
    private final JTextField tfFirst = new JTextField(12);
    private final JTextField tfLast = new JTextField(12);
    private final JTextField tfEmail = new JTextField(12);
    private final JTextField tfPhone = new JTextField(12);
    private final JTextField tfAddress = new JTextField(12);
    private final JTextField tfCreatedBy = new JTextField(12);

    // Dropdowns
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{
            "ALL", "ACTIVE", "INACTIVE", "NEW"
    });

    private final JComboBox<String> cbDept = new JComboBox<>(new String[]{
            "ALL", "Finance", "HR", "IT", "Marketing"
    });

    private final JComboBox<String> cbPosition = new JComboBox<>(new String[]{
            "ALL", "Analyst", "Recruiter", "Developer", "Manager"
    });

    public FilterPanel() {

        setBackground(new Color(0xF5F5F5));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int index = 0;

        index = addField(gbc, index, "Emp ID:", tfEmpId);
        index = addField(gbc, index, "First Name:", tfFirst);
        index = addField(gbc, index, "Last Name:", tfLast);
        index = addField(gbc, index, "Dept:", cbDept);
        index = addField(gbc, index, "Position:", cbPosition);
        index = addField(gbc, index, "Email:", tfEmail);
        index = addField(gbc, index, "Phone:", tfPhone);
        index = addField(gbc, index, "Address:", tfAddress);
        index = addField(gbc, index, "Created By:", tfCreatedBy);
        addField(gbc, index, "Status:", cbStatus);
    }

    // ---------------- UI Helper ----------------

    private int addField(GridBagConstraints gbc, int index,
                         String labelText, Component field) {

        int col = (index % 4) * 2;
        int row = index / 4;

        gbc.gridx = col;
        gbc.gridy = row;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(label, gbc);

        gbc.gridx = col + 1;
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(field, gbc);

        return index + 1;
    }

    // ---------------- CLEAR ----------------

    public void clearAll() {

        tfEmpId.setText("");
        tfFirst.setText("");
        tfLast.setText("");
        tfEmail.setText("");
        tfPhone.setText("");
        tfAddress.setText("");
        tfCreatedBy.setText("");

        cbDept.setSelectedIndex(0);
        cbPosition.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
    }

    // ---------------- HQL BUILDER ----------------

    public static class HqlCriteria {
        public String where = "";
        public List<Object> params = new ArrayList<>();
    }

    public HqlCriteria buildCriteria() {

        HqlCriteria c = new HqlCriteria();
        StringBuilder where = new StringBuilder();

        add(where, c, "e.empId = ?", tfEmpId.getText());

        addLike(where, c, "lower(e.firstName)", tfFirst.getText());
        addLike(where, c, "lower(e.lastName)", tfLast.getText());
        addLike(where, c, "lower(e.email)", tfEmail.getText());
        addLike(where, c, "lower(e.phone)", tfPhone.getText());
        addLike(where, c, "lower(e.address)", tfAddress.getText());
        addLike(where, c, "lower(e.createdBy)", tfCreatedBy.getText());

        if (!"ALL".equals(cbDept.getSelectedItem())) {
            add(where, c, "lower(e.department) = ?",
                    cbDept.getSelectedItem().toString().toLowerCase());
        }

        if (!"ALL".equals(cbPosition.getSelectedItem())) {
            add(where, c, "lower(e.position) = ?",
                    cbPosition.getSelectedItem().toString().toLowerCase());
        }

        if (!"ALL".equals(cbStatus.getSelectedItem())) {
            add(where, c, "e.status = ?", cbStatus.getSelectedItem());
        }

        if (where.length() > 0) {
            c.where = " WHERE " + where.toString();
        }

        return c;
    }

    // ---------------- QUERY HELPERS ----------------

    private void add(StringBuilder where, HqlCriteria c,
                     String clause, String value) {

        if (value != null && !value.trim().isEmpty()) {
            append(where, clause);
            c.params.add(value.trim());
        }
    }

    private void addLike(StringBuilder where, HqlCriteria c,
                         String field, String value) {

        if (value != null && !value.trim().isEmpty()) {
            append(where, field + " LIKE ?");
            c.params.add("%" + value.trim().toLowerCase() + "%");
        }
    }

    private void append(StringBuilder where, String clause) {
        if (where.length() > 0) {
            where.append(" AND ");
        }
        where.append(clause);
    }
}