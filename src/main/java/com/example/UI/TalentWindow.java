package com.example.UI;

import com.example.ExamplePlugin;
import com.example.SQLite.Database;
import com.example.SQLite.PrestigeCardData;
import java.util.List;


import javax.swing.*;
import java.awt.*;

public class TalentWindow extends JFrame {
    private ExamplePlugin plugin;
    private Database database;
    private static TalentWindow instance;
    JPanel overlayPanel = new JPanel();

    JPanel scrollablePanel = new JPanel(null); // Null layout for absolute positioning

    JScrollPane scrollPane = new JScrollPane(scrollablePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


    public TalentWindow(ExamplePlugin plugin) {
        this.plugin = plugin;
        instance = this;
        setTitle("Prestige Window");
        setSize(1080, 720); // Set the size of the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close the window on close

        // Create and add components
        initUI();
    }
    public static TalentWindow getInstance() {
        return instance; // Add a static method to retrieve the instance
    }


    private void initUI() {
        // Container panel with BorderLayout
        JPanel containerPanel = new JPanel(new BorderLayout());

        // Scrollable content panel

        scrollablePanel.setPreferredSize(new Dimension(2000, 2000)); // Adjust as necessary

        PrestigeCard startCard = null;
        List<PrestigeCardData> cardDataList = database.getPrestigeCards();

        for (PrestigeCardData cardData : cardDataList) {
            String unlocks = cardData.getCurrentUnlocks() + "/" + cardData.getMaxUnlocks();
            PrestigeCard card = new PrestigeCard(
                    plugin,
                    database,
                    cardData.getLeftTitle(),
                    unlocks,
                    "Cost: " + cardData.getCost(),
                    cardData.getDescription()
            );
            card.setBounds(new Rectangle(
                    cardData.getPosX(),
                    cardData.getPosY(),
                    cardData.getWidth(),
                    cardData.getHeight()
            ));
            scrollablePanel.add(card);

            if ("Start".equals(cardData.getLeftTitle())) {
                startCard = card;
            }
        }

        // Wrap the scrollablePanel in a JScrollPane

        containerPanel.add(scrollPane, BorderLayout.CENTER);

        // Static overlay panel


        int prestigePoints = database.getPrestigePoints();
        overlayPanel.add(new JLabel("Prestige Points: " + prestigePoints));
        overlayPanel.setOpaque(false); // Set to true if you want a solid background
        containerPanel.add(overlayPanel, BorderLayout.NORTH);

        // Add the container panel to the window
        add(containerPanel);

        // Adjust scrollPane view to center on the "Start" card
        if (startCard != null) {
            // Calculate the new view position based on the start card's position
            Rectangle startBounds = startCard.getBounds();
            int x = Math.max(0, startBounds.x + startBounds.width / 2 - getWidth() / 2);
            int y = Math.max(0, startBounds.y + startBounds.height / 2 - getHeight() / 2);
            scrollPane.getViewport().setViewPosition(new Point(x, y));
        } else {
            // Handle the case where the "Start" card is not found in the database
            // You can provide a default position or show an error message.
        }
    }

    public void refreshWindow() {
        // Clear the existing scrollablePanel content
        scrollablePanel.removeAll();
        overlayPanel.removeAll();

        int prestigePoints = database.getPrestigePoints();
        overlayPanel.add(new JLabel("Prestige Points: " + prestigePoints));
        overlayPanel.repaint();
        overlayPanel.revalidate();

        PrestigeCard startCard = null;
        List<PrestigeCardData> cardDataList = database.getPrestigeCards();

        for (PrestigeCardData cardData : cardDataList) {
            String unlocks = cardData.getCurrentUnlocks() + "/" + cardData.getMaxUnlocks();
            PrestigeCard card = new PrestigeCard(
                    plugin,
                    database,
                    cardData.getLeftTitle(),
                    unlocks,
                    "Cost: " + cardData.getCost(),
                    cardData.getDescription()
            );
            card.setBounds(new Rectangle(
                    cardData.getPosX(),
                    cardData.getPosY(),
                    cardData.getWidth(),
                    cardData.getHeight()
            ));
            scrollablePanel.add(card);

            if ("Start".equals(cardData.getLeftTitle())) {
                startCard = card;
            }
        }

        // Repaint the scrollablePanel to reflect the updated content
        scrollablePanel.repaint();

        // Adjust scrollPane view to center on the "Start" card if it exists
        if (startCard != null) {
            Rectangle startBounds = startCard.getBounds();
            int x = Math.max(0, startBounds.x + startBounds.width / 2 - getWidth() / 2);
            int y = Math.max(0, startBounds.y + startBounds.height / 2 - getHeight() / 2);
            scrollPane.getViewport().setViewPosition(new Point(x, y));
        } else {
            // Handle the case where the "Start" card is not found in the database
            // You can provide a default position or show an error message.
        }
    }
}