package com.bnpparibas.ui;

import javax.swing.*;
import java.awt.*;

import com.bnpparibas.util.SessionManager;

public class TopPanel extends JPanel {

    public TopPanel(String currentUser,
                    FilterPanel filterPanel,
                    ActionPanel actionPanel) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(0xF5F5F5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // -------- Row 1: Logout --------
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(getBackground());

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);

        logoutBtn.addActionListener(e -> {
            SessionManager.logout(currentUser); // IMPORTANT FIX
            new LoginUI();
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        logoutPanel.add(logoutBtn);
        add(logoutPanel);

        // -------- Row 2: Filters --------
        add(filterPanel);

        // -------- Row 3: Actions --------
        add(actionPanel);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(0x004170));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}