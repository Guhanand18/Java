package com.bnpparibas.ui;

import javax.swing.*;
import java.awt.*;

public class ActionPanel extends JPanel {

    private final JButton importBtn = new JButton("Import File");
    private final JButton searchBtn = new JButton("Search");
    private final JButton clearBtn = new JButton("Clear");
    private final JButton exportBtn = new JButton("Export CSV");

    public ActionPanel() {

        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setBackground(new Color(0xF5F5F5));

        style(importBtn);
        style(searchBtn);
        style(clearBtn);
        style(exportBtn);

        add(importBtn);
        add(searchBtn);
        add(clearBtn);
        add(exportBtn);
    }

    public JButton getImportButton() { return importBtn; }
    public JButton getSearchButton() { return searchBtn; }
    public JButton getClearButton() { return clearBtn; }
    public JButton getExportButton() { return exportBtn; }

    private void style(JButton btn) {
        btn.setBackground(new Color(0x004170));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}