package com.example.SQLite;

public class PrestigeCardData {
    private String leftTitle;
    private int currentUnlocks;
    private int maxUnlocks;
    private int cost;
    private String description;
    private int posX;
    private int posY;
    private int width;
    private int height;

    // Constructor
    public PrestigeCardData(String leftTitle, int currentUnlocks,int maxUnlocks, int cost, String description, int posX, int posY, int width, int height) {
        this.leftTitle = leftTitle;
        this.currentUnlocks = currentUnlocks;
        this.maxUnlocks = maxUnlocks;
        this.cost = cost;
        this.description = description;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    // Getters
    public String getLeftTitle() {
        return leftTitle;
    }

    public int getCurrentUnlocks() {
        return currentUnlocks;
    }

    public int getMaxUnlocks() {
        return maxUnlocks;
    }

    public int getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public void setCurrentUnlocks(int currentUnlocks) {
        this.currentUnlocks = currentUnlocks;
    }
}
