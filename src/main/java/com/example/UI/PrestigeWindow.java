package com.example.UI;

import com.example.ExamplePlugin;
import com.example.SQLite.Database;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class PrestigeWindow extends JFrame {
    private ExamplePlugin plugin;
    public PrestigeWindow(ExamplePlugin plugin) {
        this.plugin = plugin;
        setTitle("Confirm starting position");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Label
        JLabel label = new JLabel("<html>Are you sure you want to start here?<br> This will remove all tiles except a 3x3 grid around your current position.<br></html>", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // Prestige button
        JButton prestigeButton = new JButton("OK");
        buttonPanel.add(prestigeButton);
        prestigeButton.addActionListener(l -> {
            plugin.clearTiles();
        });
        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}
