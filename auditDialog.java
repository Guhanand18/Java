package com.bnpparibas.ui;

import com.bnpparibas.dao.AuditDAO;
import com.bnpparibas.model.Audit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class AuditDialog extends JDialog {

    private final AuditDAO auditDAO = new AuditDAO();

    public AuditDialog(Frame owner, String empId) {
        super(owner, "Audit - Employee " + empId, true);

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        String[] cols = {"Version", "Modified By", "Modified At"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        List<Audit> audits = auditDAO.getAuditsForEmp(empId);

        if (audits == null || audits.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No audit records found");
            dispose();
            return;
        }

        for (Audit a : audits) {
            model.addRow(new Object[]{
                    a.getVersionNumber(),
                    a.getModifiedBy(),
                    a.getModifiedAt()
            });
        }

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {

                    int row = table.getSelectedRow();
                    if (row < 0) return;

                    Long auditId = audits.get(row).getId();
                    new AuditDetailDialog(AuditDialog.this, auditId);
                }
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        add(closeBtn, BorderLayout.SOUTH);

        setVisible(true);
    }
}