package com.bnpparibas.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

import com.bnpparibas.model.Employee;
import com.bnpparibas.model.ImportResult;
import com.bnpparibas.service.FileService;
import com.bnpparibas.util.HibernateUtil;

public class EmployeeTableUI extends JFrame {

    private final String currentUser;
    private final String role;

    private final FilterPanel filterPanel;
    private final TablePanel tablePanel;
    private final NavigationPanel navPanel;
    private final ActionPanel actionPanel;
    private final TopPanel topPanel;

    private int page = 0;
    private static final int PAGE_SIZE = 20;
    private int totalPages = 1;

    public EmployeeTableUI(String currentUser, String role) {
        this.currentUser = currentUser;
        this.role = role;

        filterPanel = new FilterPanel();
        tablePanel = new TablePanel();
        navPanel = new NavigationPanel();
        actionPanel = new ActionPanel();
        topPanel = new TopPanel(currentUser, filterPanel, actionPanel);

        setTitle("Employee Table - " + currentUser);
        setSize(1150, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);

        // Navigation
        navPanel.getPrevButton().addActionListener(e -> prevPage());
        navPanel.getNextButton().addActionListener(e -> nextPage());

        // Actions
        actionPanel.getImportButton().addActionListener(e -> importFile());
        actionPanel.getSearchButton().addActionListener(e -> search());
        actionPanel.getClearButton().addActionListener(e -> clearFilters());
        actionPanel.getExportButton().addActionListener(e -> exportCsv());

        // Double click
        tablePanel.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tablePanel.getTable().getSelectedRow();
                    if (row >= 0) {
                        String empId = tablePanel.getTable()
                                .getValueAt(row, 0).toString();
                        showOptions(empId);
                    }
                }
            }
        });

        loadData();
        setVisible(true);
    }

    // ---------------- Pagination ----------------

    private void prevPage() {
        if (page > 0) {
            page--;
            loadData();
        }
    }

    private void nextPage() {
        if (page < totalPages - 1) {
            page++;
            loadData();
        }
    }

    // ---------------- Filters ----------------

    private void search() {
        page = 0;
        loadData();
    }

    private void clearFilters() {
        filterPanel.clearAll();
        page = 0;
        loadData();
    }

    // ---------------- Load Data ----------------

    private void loadData() {

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            long totalRows = countRows();
            totalPages = (int) Math.ceil(totalRows / (double) PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            FilterPanel.HqlCriteria crit = filterPanel.buildCriteria();

            String hql = "from Employee e " + crit.where + " order by e.empId";

            Query q = session.createQuery(hql);
            q.setFirstResult(page * PAGE_SIZE);
            q.setMaxResults(PAGE_SIZE);

            for (int i = 0; i < crit.params.size(); i++) {
                q.setParameter(i, crit.params.get(i));
            }

            List<Employee> list = q.list();

            tablePanel.refresh(list);
            navPanel.update(page, totalPages);

        } finally {
            session.close();
        }
    }

    private long countRows() {

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            FilterPanel.HqlCriteria crit = filterPanel.buildCriteria();

            Query q = session.createQuery(
                    "select count(e.empId) from Employee e " + crit.where);

            for (int i = 0; i < crit.params.size(); i++) {
                q.setParameter(i, crit.params.get(i));
            }

            Long count = (Long) q.uniqueResult();
            return count == null ? 0 : count;

        } finally {
            session.close();
        }
    }

    // ---------------- Import ----------------

    private void importFile() {

        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        FileService service = new FileService();
        ImportResult result = service.processFile(file, currentUser);

        JOptionPane.showMessageDialog(this,
                "Import Done\nValid: " + result.getValid()
                        + "\nInvalid: " + result.getInvalid());

        loadData();
    }

    // ---------------- Export ----------------

    private void exportCsv() {
        CsvExporter.export(this, filterPanel.buildCriteria());
    }

    // ---------------- Options ----------------

    private void showOptions(String empId) {

        java.util.List<String> options = new java.util.ArrayList<>();
        options.add("View");

        if (role.contains("EDIT")) options.add("Edit");
        if (role.contains("VALIDATE")) options.add("Validate");

        options.add("Audit");

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Choose action",
                "Options",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options.toArray(),
                options.get(0)
        );

        if (choice == null) return;

        switch (choice) {
            case "View":
                viewEmployee(empId);
                break;
            case "Edit":
                new EditEmployeeUI(empId, this, currentUser);
                break;
            case "Validate":
                validateEmployee(empId);
                break;
            case "Audit":
                new AuditDialog(this, empId);
                break;
        }
    }

    // ---------------- View ----------------

    private void viewEmployee(String empId) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Employee emp = (Employee) session.get(Employee.class, empId);

            JOptionPane.showMessageDialog(this,
                    "ID: " + emp.getEmpId() +
                            "\nName: " + emp.getFirstName() + " " + emp.getLastName() +
                            "\nDept: " + emp.getDepartment());
        } finally {
            session.close();
        }
    }

    // ---------------- Validate ----------------

    private void validateEmployee(String empId) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            Employee emp = (Employee) session.get(Employee.class, empId);

            if (currentUser.equals(emp.getLastModifiedBy())) {
                JOptionPane.showMessageDialog(this,
                        "Cannot validate your own changes");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Confirm validation?");

            if (confirm != JOptionPane.YES_OPTION) return;

            emp.setStatus("VALIDATED");
            session.update(emp);

            tx.commit();

            JOptionPane.showMessageDialog(this, "Validated!");
            loadData();

        } catch (Exception e) {
            tx.rollback();
        } finally {
            session.close();
        }
    }

    // ---------------- Refresh ----------------

    public void refreshAfterEdit() {
        loadData();
    }
}