package com.example;
import com.example.UI.PrestigeWindow;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.swing.JPanel;


@Slf4j
@Singleton
public class ExamplePanel extends PluginPanel {
    private final ExamplePlugin plugin;

    public ExamplePanel(ExamplePlugin plugin) {
        this.plugin = plugin;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        JLabel title = new JLabel();
        title.setText("Tileman (blackout edition)");
        title.setForeground(Color.WHITE);

        northPanel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.setLayout(new GridLayout(0, 1));

        JLabel info = new JLabel(htmlLabel("Clicking the Active/Inactive button allow you to toggle redrawing the scene when a tile is unlocked. Can be useful when bossing/skilling etc. ", "#FFFFFF"));

        //JLabel warning = new JLabel(htmlLabel("WARNING: Choose power-ups carefully and be in a good position to get XP, as you might get soft/hard locked if you prestige in a bad position.", "#FFFF00"));

        infoPanel.add(info);
        //infoPanel.add(warning);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        /*    //was used for prestige code
        JButton talentButton = new JButton("Talent");
        centerPanel.add(talentButton, BorderLayout.SOUTH);
        talentButton.addActionListener(l -> {
            TalentWindow talentWindow = new TalentWindow(plugin);
            talentWindow.setLocationRelativeTo(null); // Center the window
            talentWindow.setVisible(true);
        });

         */

        JButton talentButton = new JButton("Active");
        talentButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        centerPanel.add(talentButton, BorderLayout.SOUTH);

        talentButton.addActionListener(l -> {
            if (talentButton.getText().equals("Inactive")) {
                talentButton.setText("Active");
                plugin.unlockTilesEnabled = true;
                talentButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            } else {
                talentButton.setText("Inactive");
                plugin.unlockTilesEnabled = false;
                talentButton.setBackground(Color.RED);
            }
        });

        JButton prestigeButton = new JButton("Set start");
        centerPanel.add(prestigeButton, BorderLayout.SOUTH);
        prestigeButton.addActionListener(l -> {
            if (plugin.checkStarted() == 0) {
                PrestigeWindow prestigeWindow = new PrestigeWindow(plugin);
                prestigeWindow.setLocationRelativeTo(null); // Center the window
                prestigeWindow.setVisible(true);
            }
        });



        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        talentButton.setToolTipText("Open Talent window");
        prestigeButton.setToolTipText("Do you want to prestige?");
        centerPanel.setLayout(new FlowLayout());
        centerPanel.add(talentButton);
        centerPanel.add(prestigeButton);
        add(northPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.SOUTH);

        // Create a new panel for the directional buttons with a 3x3 grid layout
        JPanel directionPanel = new JPanel(new GridLayout(3, 3));
        directionPanel.setBackground(ColorScheme.DARK_GRAY_COLOR); // Set background to match your theme

// Create placeholders for the grid
        JLabel empty1 = new JLabel();
        JLabel empty2 = new JLabel();
        JLabel empty3 = new JLabel();
        JLabel empty4 = new JLabel();

// Create the directional buttons
        JButton northButton = new JButton("N");
        northButton.addActionListener(l -> {
            plugin.unlockDirection(northButton.getText());
        });

        JButton westButton = new JButton("W");
        westButton.addActionListener(l -> {
            plugin.unlockDirection(westButton.getText());
        });
        JButton eastButton = new JButton("E");
        eastButton.addActionListener(l -> {
            plugin.unlockDirection(eastButton.getText());
        });
        JButton southButton = new JButton("S");
        southButton.addActionListener(l -> {
            plugin.unlockDirection(southButton.getText());
        });


// Add components to the grid in the order to make the layout xNx, WxE, xSx
        directionPanel.add(empty1);
        directionPanel.add(northButton);
        directionPanel.add(empty2);
        directionPanel.add(westButton);
        directionPanel.add(empty3);
        directionPanel.add(eastButton);
        directionPanel.add(empty4);
        directionPanel.add(southButton);

// Add the directional panel to the main panel, adjusting the layout as needed
        add(directionPanel, BorderLayout.AFTER_LAST_LINE);


        mainPanel.add(centerPanel);

// Add the directionPanel to the mainPanel
        mainPanel.add(directionPanel);

// Add the mainPanel to the ExamplePanel
        add(mainPanel, BorderLayout.SOUTH);
    }


    private static String htmlLabel(String key, String color)
    {
        return "<html><body style = 'color:" + color + "'>" + key + "</body></html>";
    }
}