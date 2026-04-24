package com.bnpparibas.ui;

import com.bnpparibas.dao.AuditDAO;
import com.bnpparibas.model.AuditDetail;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditDetailDialog extends JDialog {

    private final AuditDAO auditDAO = new AuditDAO();

    public AuditDetailDialog(Window owner, Long auditId) {
        super(owner, "Audit Details", ModalityType.APPLICATION_MODAL);
        init(auditId);
    }

    private void init(Long auditId) {

        setSize(540, 380);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(5, 5));

        String[] cols = {"Attribute", "Old Value", "New Value"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        List<AuditDetail> details = auditDAO.getDetailsForAudit(auditId);

        if (details == null || details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No audit details found");
        } else {
            for (AuditDetail d : details) {
                model.addRow(new Object[]{
                        d.getAttribute(),
                        d.getOldValue(),
                        d.getNewValue()
                });
            }
        }

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeBtn);

        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }
}