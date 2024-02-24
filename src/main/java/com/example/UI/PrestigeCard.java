package com.example.UI;

import com.example.ExamplePlugin;
import com.example.SQLite.Database;
import net.runelite.client.ui.ColorScheme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Objects;

public class PrestigeCard extends JPanel {
    private ExamplePlugin plugin;
    private Database database;
    private JPanel titlePanel;
    private JLabel leftTitleLabel;
    private JLabel rightTitleLabel;
    private JLabel costLabel;
    private JTextArea descriptionArea;

    public PrestigeCard(ExamplePlugin plugin, Database database, String leftTitle, String rightTitle, String cost, String description) {
        this.plugin = plugin;
        this.database = database;
        setBackground(ColorScheme.DARKER_GRAY_COLOR); // Or any other background color
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;


        // Add mouse listener for showing the popup menu
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Get the leftTitle of the clicked card
                    String cardLeftTitle = leftTitle;

                    //JPopupMenu popupMenu = createPopupMenu(cardLeftTitle);
                    //popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });


        // Title Panel with two labels
        titlePanel = new JPanel(new BorderLayout());
        leftTitleLabel = new JLabel(leftTitle);
        rightTitleLabel = new JLabel(rightTitle);
        titlePanel.add(leftTitleLabel, BorderLayout.WEST);
        titlePanel.add(rightTitleLabel, BorderLayout.EAST);

        add(titlePanel, gbc);

        costLabel = new JLabel(htmlLabel(cost, "#FFFFFF"));
        costLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(costLabel, gbc);

        descriptionArea = new JTextArea(2, 20); // Adjust rows and columns as needed
        descriptionArea.setText(description);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        descriptionArea.setForeground(Color.WHITE);
        descriptionArea.setFont(new Font("Dialog", Font.PLAIN, 12)); // Set the desired font

        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(descriptionArea, gbc);
    }

    private static String htmlLabel(String text, String color) {
        return "<html><body style='color:" + color + "'>" + text + "</body></html>";
    }
/*
    private JPopupMenu createPopupMenu(String cardLeftTitle) {
        JPopupMenu popup = new JPopupMenu();
        int cost = database.getCardCost(cardLeftTitle);

        // Example menu items
        JMenuItem unlockMenuItem = new JMenuItem("Unlock");
        unlockMenuItem.addActionListener(e -> {
            database.updateCurrentUnlocks(cardLeftTitle, true, cost); // Pass true for unlock
            TalentWindow prestigeWindow = TalentWindow.getInstance();
            System.out.println(cardLeftTitle + " " + database.getStarted());
            if (Objects.equals(cardLeftTitle, "Start") && database.getStarted() == 0)
            {
                //database.setStarted();
                plugin.clearTiles();
                System.out.println("Variable started set to: " + database.getStarted());

            }
            if (prestigeWindow != null) {
                prestigeWindow.refreshWindow();
            }
        });
        popup.add(unlockMenuItem);

        if(!Objects.equals(cardLeftTitle, "Start")) {
            JMenuItem refundMenuItem = new JMenuItem("Refund");
            refundMenuItem.addActionListener(e -> {
                database.updateCurrentUnlocks(cardLeftTitle, false, cost); // Pass false for refund
                TalentWindow prestigeWindow = TalentWindow.getInstance();
                if (prestigeWindow != null) {
                    prestigeWindow.refreshWindow();
                }
            });
            popup.add(refundMenuItem);
        }
        return popup;
    }

 */
}