package com.bnpparibas.ui;

import javax.swing.*;
import java.awt.*;

public class NavigationPanel extends JPanel {

    private final JButton prevBtn = new JButton("Prev");
    private final JButton nextBtn = new JButton("Next");
    private final JLabel infoLabel = new JLabel("Page 1 of 1");

    public NavigationPanel() {

        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        setBackground(new Color(0xF5F5F5));

        style(prevBtn);
        style(nextBtn);

        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        add(prevBtn);
        add(infoLabel);
        add(nextBtn);
    }

    public JButton getPrevButton() { return prevBtn; }
    public JButton getNextButton() { return nextBtn; }

    public void update(int page, int totalPages) {

        prevBtn.setEnabled(page > 0);
        nextBtn.setEnabled(page < totalPages - 1);

        infoLabel.setText("Page " + (page + 1) + " of " + totalPages);
    }

    private void style(JButton btn) {
        btn.setBackground(new Color(0x004170));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}