package com.example.SQLite;

public class UserStatsData {
    private int playerTiles;
    private int randomTiles;
    private int bonusTiles;
    private int started;

    // Constructor
    public UserStatsData(int playerTiles, int randomTiles, int bonusTiles, int started) {

        this.playerTiles = playerTiles;
        this.randomTiles = randomTiles;
        this.bonusTiles = bonusTiles;
        this.started = started;
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