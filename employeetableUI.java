package com.bnpparibas.ui;

import javax.swing.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.bnpparibas.model.Employee;
import com.bnpparibas.util.HibernateUtil;

import java.awt.*;
import java.io.File;
import java.util.List;

public class EmployeeTableUI extends JFrame {

    private final String currentUser;

    private FilterPanel filterPanel;
    private ActionPanel actionPanel;
    private TablePanel tablePanel;
    private NavigationPanel navigationPanel;

    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalPages = 0;

    public EmployeeTableUI(String currentUser) {
        this.currentUser = currentUser;

        setTitle("Employee Management");
        setSize(1100, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panels
        filterPanel = new FilterPanel();
        actionPanel = new ActionPanel();
        tablePanel = new TablePanel();
        navigationPanel = new NavigationPanel();

        TopPanel topPanel = new TopPanel(currentUser, filterPanel, actionPanel);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);

        attachActions();

        loadData();

        setVisible(true);
    }

    // ==============================
    // ACTIONS
    // ==============================
    private void attachActions() {

        actionPanel.getSearchButton().addActionListener(e -> {
            currentPage = 0;
            loadData();
        });

        actionPanel.getClearButton().addActionListener(e -> {
            filterPanel.clearAll();
            currentPage = 0;
            loadData();
        });

        actionPanel.getExportButton().addActionListener(e -> exportCSV());

        navigationPanel.getPrevButton().addActionListener(e -> {
            currentPage--;
            loadData();
        });

        navigationPanel.getNextButton().addActionListener(e -> {
            currentPage++;
            loadData();
        });

        // Double click → Edit
        tablePanel.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tablePanel.getTable().getSelectedRow();
                    String empId = tablePanel.getTable().getValueAt(row, 0).toString();
                    new EditEmployeeUI(empId, EmployeeTableUI.this, currentUser);
                }
            }
        });
    }

    // ==============================
    // LOAD DATA (Pagination + Filter)
    // ==============================
    private void loadData() {

        Session session = HibernateUtil.getSessionFactory().openSession();

        FilterPanel.HqlCriteria crit = filterPanel.buildCriteria();

        String baseQuery = "FROM Employee e";
        String countQuery = "SELECT COUNT(*) FROM Employee e";

        String where = crit.where.toString();

        Query<Employee> q = session.createQuery(baseQuery + where, Employee.class);
        Query<Long> countQ = session.createQuery(countQuery + where, Long.class);

        for (int i = 0; i < crit.params.size(); i++) {
            q.setParameter(i + 1, crit.params.get(i));
            countQ.setParameter(i + 1, crit.params.get(i));
        }

        // total count
        long total = countQ.uniqueResult();
        totalPages = (int) Math.ceil((double) total / pageSize);

        // pagination
        q.setFirstResult(currentPage * pageSize);
        q.setMaxResults(pageSize);

        List<Employee> rows = q.list();

        tablePanel.refresh(rows);
        navigationPanel.update(currentPage, totalPages);

        session.close();
    }

    // ==============================
    // CSV EXPORT
    // ==============================
    private void exportCSV() {

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("employees.csv"));

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try {
            List<Employee> allData = fetchAllData();
            CSVExporter.export(file, allData);

            JOptionPane.showMessageDialog(this, "Export successful!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }

    private List<Employee> fetchAllData() {

        Session session = HibernateUtil.getSessionFactory().openSession();

        FilterPanel.HqlCriteria crit = filterPanel.buildCriteria();

        String query = "FROM Employee e " + crit.where.toString();

        Query<Employee> q = session.createQuery(query, Employee.class);

        for (int i = 0; i < crit.params.size(); i++) {
            q.setParameter(i + 1, crit.params.get(i));
        }

        List<Employee> list = q.list();
        session.close();

        return list;
    }

    // ==============================
    // REFRESH AFTER EDIT
    // ==============================
    public void refreshAfterEdit() {
        loadData();
    }
}