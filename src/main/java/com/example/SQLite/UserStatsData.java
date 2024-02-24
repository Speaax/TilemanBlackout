package com.example.SQLite;

public class UserStatsData {
    private int prestigePoints;
    private int prestigeXP;
    private int currentXP;
    private int xpForNextTile;
    private int playerTiles;
    private int randomTiles;
    private int bonusTiles;
    private int started;

    // Constructor
    public UserStatsData(int prestigePoints, int prestigeXP, int currentXP, int xpForNextTile, int playerTiles, int randomTiles, int bonusTiles, int started) {
        this.prestigePoints = prestigePoints;
        this.prestigeXP = prestigeXP;
        this.currentXP = currentXP;
        this.xpForNextTile = xpForNextTile;
        this.playerTiles = playerTiles;
        this.randomTiles = randomTiles;
        this.bonusTiles = bonusTiles;
        this.started = started;
    }

    // Getters
    public int getPrestigePoints() {
        return prestigePoints;
    }

    public int getPrestigeXP() {
        return prestigeXP;
    }

    public int getCurrentXP() {
        return currentXP;
    }

    public int getXpForNextTile() {
        return xpForNextTile;
    }

    public int getPlayerTiles() {
        return playerTiles;
    }

    public int getRandomTiles() {
        return randomTiles;
    }
    public int getBonusTiles() {
        return bonusTiles;
    }

    public int getStarted() {
        return started;
    }
}