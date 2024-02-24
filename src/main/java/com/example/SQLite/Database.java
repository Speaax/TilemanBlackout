package com.example.SQLite;

import com.example.ExampleConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.runelite.api.coords.WorldPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Database {
    private ExampleConfig config;
    private HikariDataSource dataSourceMySQL;

    public Database(ExampleConfig config) {
        this.config = config;
        initializeDataSource();
    }

    private void initializeDataSource() {
        HikariConfig configMySQLDB = new HikariConfig();
        configMySQLDB.setJdbcUrl("jdbc:mysql://" + this.config.ip() + "/tileman");
        configMySQLDB.setUsername(this.config.username());
        configMySQLDB.setPassword(this.config.password());
        dataSourceMySQL = new HikariDataSource(configMySQLDB);
    }

    public Connection getConnection() throws SQLException {
        return dataSourceMySQL.getConnection();
    }

    public void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("Initialize DB");

            // Create PrestigeCards table
            String sql = "CREATE TABLE IF NOT EXISTS prestigecards (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "leftTitle VARCHAR(255) NOT NULL," +
                    "currentUnlocks INT NOT NULL," +
                    "maxUnlocks INT NOT NULL," +
                    "cost INT NOT NULL," +
                    "description VARCHAR(255) NOT NULL," +
                    "posX INT NOT NULL," +
                    "posY INT NOT NULL," +
                    "width INT NOT NULL," +
                    "height INT NOT NULL" +
                    ");";
            stmt.execute(sql);

            // Create UserStats table
            sql = "CREATE TABLE IF NOT EXISTS userstats (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "prestigePoints INT NOT NULL," +
                    "prestigeXP INT NOT NULL," +
                    "currentXP INT NOT NULL," +
                    "xpForNextTile INT NOT NULL," +
                    "playerTiles INT NOT NULL," +
                    "randomTiles INT NOT NULL," +
                    "bonusTiles INT NOT NULL," +
                    "started INT NOT NULL" +
                    ");";
            stmt.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS tiles (" +
                    "x INT NOT NULL," +
                    "y INT NOT NULL," +
                    "plane INT NOT NULL," +
                    "status INT NOT NULL," +
                    "player INT NOT NULL," +
                    "PRIMARY KEY (x, y, plane));";
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("Initialize error: " + e.getMessage());
        }
    }

    public void updateUserStats(int playerTiles, int randomTiles, int bonusTiles, int xpForNextTile, int currentXP, int playerId) {
        String sql = "UPDATE userstats SET playerTiles = ?, randomTiles = ?, bonusTiles = ?, xpForNextTile = ?, currentXP = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerTiles);
            pstmt.setInt(2, randomTiles);
            pstmt.setInt(3, bonusTiles);
            pstmt.setInt(4, xpForNextTile);
            pstmt.setInt(5, currentXP);
            pstmt.setInt(6, playerId); // Use config.playerID() value to identify the row

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating user stats: " + e.getMessage());
        }
    }


    public void insertOrUpdateTile(int x, int y, int plane, int status, int player) {
        String sql = "REPLACE INTO tiles (x, y, plane, status, player) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setInt(3, plane);
            pstmt.setInt(4, status);
            pstmt.setInt(5, player);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting or updating tile: " + e.getMessage());
        }
    }

    public void prestigeUpdate(int additionalPrestigePoints, int prestigeXP) {
        String selectSql = "SELECT prestigePoints FROM userstats WHERE id = 1";
        // Correct the syntax error in the update statement
        String updateSql = "UPDATE userstats SET prestigePoints = ?, prestigeXP = ? WHERE id IN (1, 2)";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Fetch current prestigePoints
            int currentPrestigePoints = 0;
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                currentPrestigePoints = rs.getInt("prestigePoints");
            }

            // Add additionalPrestigePoints to currentPrestigePoints
            int newPrestigePoints = currentPrestigePoints + additionalPrestigePoints;

            // Set values for the update statement
            updateStmt.setInt(1, newPrestigePoints);
            updateStmt.setInt(2, prestigeXP);

            updateStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating user stats: " + e.getMessage());
        }
    }


    public void clearTilesByStatus(int status) {
        String sql = "DELETE FROM tiles WHERE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error clearing tiles: " + e.getMessage());
        }
    }

    public List<WorldPoint> getTilesByStatus(int status) {
        List<WorldPoint> tiles = new ArrayList<>();
        String sql = "SELECT x, y, plane FROM tiles WHERE status = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int plane = rs.getInt("plane");
                    tiles.add(new WorldPoint(x, y, plane));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tiles by status: " + e.getMessage());
        }

        return tiles;
    }

    public int countTilesByStatus(int status) {
        String sql = "SELECT COUNT(*) FROM tiles WHERE status = ?";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting tiles by status: " + e.getMessage());
        }

        return count;
    }

    public int countTilesByPlayerAndStatus(int player, int status) {
        String sql = "SELECT COUNT(*) FROM tiles WHERE player = ? AND status = ?";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, player);
            pstmt.setInt(2, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting tiles by player and status: " + e.getMessage());
        }

        return count;
    }



    public int getTileStatus(int x, int y, int plane) {
        String sql = "SELECT status FROM tiles WHERE x = ? AND y = ? AND plane = ?";
        int status = -1; // Default value indicating no status or not found

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setInt(3, plane);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    status = rs.getInt("status");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tile status: " + e.getMessage());
        }

        return status; // Returns the status of the tile, or -1 if not found or an error occurs
    }






    public void setupInitialCards () throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // List of initial card data
            List<PrestigeCardData> initialCards = Arrays.asList(
                    new PrestigeCardData("Start", 0, 1, 0, "Unlock a random tile every time you get 1000 XP", 1000, 1000, 300, 100),
                    new PrestigeCardData("Prepare for trouble", 0, 4, 1000, "+25% chance to unlock an additional tile for each point spent on this talent.", 1000, 1140, 300, 100),
                    new PrestigeCardData("And make it double", 0, 1, 5000, "Doubles the % chance to unlock extra tiles(can exceed 100% for multiple tiles)", 1000, 1280, 300, 100),
                    new PrestigeCardData("test", 0, 1, 5000, "Doubles the % chance to unlock extra tiles(can exceed 100% for multiple tiles)", 1000, 1280, 300, 100)
                    // ... Add other initial cards ...
            );

            for (PrestigeCardData card : initialCards) {
                String checkSql = "SELECT COUNT(*) FROM PrestigeCards WHERE leftTitle = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, card.getLeftTitle());
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next() || rs.getInt(1) == 0) {
                        insertPrestigeCard(card.getLeftTitle(), card.getCurrentUnlocks(), card.getMaxUnlocks(), card.getCost(), card.getDescription(), card.getPosX(), card.getPosY(), card.getWidth(), card.getHeight());
                    }
                } catch (SQLException e) {
                    System.out.println("Error checking for card existence: " + e.getMessage());
                }
            }
        }
    }

        public void setupInitialUserStats () {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT COUNT(*) FROM userstats";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Initial user stats already exist, skipping setup.");
                return;
            }
            // List of initial user statistics data
            List<UserStatsData> initialUserStats = Arrays.asList(
                    new UserStatsData(1000, 0, 0, 1000, 0, 0, 0, 0), // Adjust values as needed
                    new UserStatsData(1000, 0, 0, 1000, 0, 0, 0, 0) // Adjust values as needed
            );
            for (UserStatsData userStats : initialUserStats) {
                insertUserStats(userStats.getPrestigePoints(), userStats.getPrestigeXP(), userStats.getCurrentXP(), userStats.getXpForNextTile(), userStats.getPlayerTiles(), userStats.getRandomTiles(), userStats.getBonusTiles(), userStats.getStarted());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


        public void insertPrestigeCard (String leftTitle,int currentUnlocks, int maxUnlocks, int cost, String
            description,int posX, int posY, int width, int height){
        String sql = "INSERT INTO prestigecards(leftTitle, currentUnlocks, maxUnlocks, cost, description, posX, posY, width, height) VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection conn = getConnection(); // Get a connection from the connection pool
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, leftTitle);
            pstmt.setInt(2, currentUnlocks);
            pstmt.setInt(3, maxUnlocks);
            pstmt.setInt(4, cost);
            pstmt.setString(5, description);
            pstmt.setInt(6, posX);
            pstmt.setInt(7, posY);
            pstmt.setInt(8, width);
            pstmt.setInt(9, height);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertUserStats ( int prestigePoints, int prestigeXP, int currentXP, int xpForNextTile,
                                         int playerTiles, int randomTiles, int bonusTiles, int started){

        String sql = "INSERT INTO userstats(prestigePoints, prestigeXP, currentXP, xpForNextTile, playerTiles, randomTiles, bonusTiles, started) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = getConnection(); // Get a connection from the connection pool
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prestigePoints);
            pstmt.setInt(2, prestigeXP);
            pstmt.setInt(3, currentXP);
            pstmt.setInt(4, xpForNextTile);
            pstmt.setInt(5, playerTiles);
            pstmt.setInt(6, randomTiles);
            pstmt.setInt(7, bonusTiles);
            pstmt.setInt(8, started);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public List<PrestigeCardData> getPrestigeCards () {
        List<PrestigeCardData> cards = new ArrayList<>();
        String sql = "SELECT * FROM prestigecards";

        try (Connection conn = getConnection(); // Get a connection from the connection pool
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PrestigeCardData card = new PrestigeCardData(
                        rs.getString("leftTitle"),
                        rs.getInt("currentUnlocks"),
                        rs.getInt("maxUnlocks"),
                        rs.getInt("cost"),
                        rs.getString("description"),
                        rs.getInt("posX"),
                        rs.getInt("posY"),
                        rs.getInt("width"),
                        rs.getInt("height")
                );
                cards.add(card);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cards;
    }


    public void updateCurrentUnlocks (String leftTitle,boolean isUnlock, int cost){
        String selectSql = "SELECT currentUnlocks, maxUnlocks FROM prestigecards WHERE leftTitle = ?";
        String updateSql = "UPDATE prestigecards SET currentUnlocks = ? WHERE leftTitle = ?";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Get the currentUnlocks and maxUnlocks values from the database
            selectStmt.setString(1, leftTitle);
            ResultSet resultSet = selectStmt.executeQuery();

            int currentPrestigePoints = getPrestigePoints();
            if (isUnlock && cost > currentPrestigePoints) {
                System.out.println("Insufficient Prestige Points. Cannot unlock.");
                return;
            }
            if (resultSet.next()) {
                int currentUnlocks = resultSet.getInt("currentUnlocks");
                int maxUnlocks = resultSet.getInt("maxUnlocks");

                if (isUnlock) {
                    if (currentUnlocks < maxUnlocks) {
                        currentUnlocks++;
                    } else {
                        System.out.println("Maximum unlocks reached, cannot unlock further.");
                        return;
                    }
                } else {
                    if (currentUnlocks > 0) {
                        currentUnlocks--;
                    } else {
                        System.out.println("No unlocks to refund.");
                        return;
                    }
                }
                updatePrestigePoints(conn, cost, isUnlock);
                // Update the database with the new currentUnlocks value
                updateStmt.setInt(1, currentUnlocks);
                updateStmt.setString(2, leftTitle);
                updateStmt.executeUpdate();

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // Handle exceptions as needed
        }
    }


    public int getPrestigePoints() {
        int prestigePoints = 0; // Default value in case of any issues

        String selectPrestigePointsSql = "SELECT prestigePoints FROM userstats";

        try (Connection conn = getConnection()) { // Use the connection from the pool
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectPrestigePointsSql)) {

                if (rs.next()) {
                    prestigePoints = rs.getInt("prestigePoints");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // Handle exceptions as needed
        }

        return prestigePoints;
    }

    public void updatePrestigePoints (Connection conn,int cost, boolean isUnlock){
        String sql = "UPDATE userstats SET prestigePoints = prestigePoints + ?";
        int pointsChange = isUnlock ? -cost : cost;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pointsChange);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating prestige points: " + e.getMessage());
        }
    }


    public int getCardCost(String cardLeftTitle){
        String sql = "SELECT cost FROM prestigecards WHERE leftTitle = ?";
        int cost = 0; // Default cost in case the card is not found

        try (Connection conn = getConnection()) { // Use the connection from the pool
            conn.setAutoCommit(false); // Start a transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cardLeftTitle);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    cost = resultSet.getInt("cost");
                }
            }

            conn.commit(); // Commit the transaction
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // Rollback and handle exceptions as needed
        }

        return cost;
    }

    public int getTotalTiles() {
        String sql = "SELECT playerTiles FROM userstats WHERE id = 1"; // Assuming 'id = 1' is the row you want to query
        int totalTiles = 0;

        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                totalTiles = rs.getInt("playerTiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching total tiles: " + e.getMessage());
        }

        return totalTiles;
    }
    public int getCurrentXP() {
        String sql = "SELECT currentXP FROM userstats WHERE id = 1"; // Assuming 'id = 1' is the row you want to query
        int overallXP = 0;

        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                overallXP = rs.getInt("currentXP");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching total tiles: " + e.getMessage());
        }

        return overallXP;
    }

    public int getPrestigeXP(int playerId) {
        int prestigeXP = 0;
        String sql = "SELECT prestigeXP FROM userstats WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prestigeXP = rs.getInt("prestigeXP");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching Prestige XP: " + e.getMessage());
        }

        return prestigeXP;
    }

    public int getStarted(int playerID) {
        String sql = "SELECT started FROM userstats WHERE id = ?"; // Use prepared statement parameter

        int started = 0;
        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerID); // Set the parameter value
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    started = rs.getInt("started");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching started: " + e.getMessage());
        }

        return started;
    }

    public void setStarted(int playerID) {
        String sql = "UPDATE userstats SET started = 1 WHERE id = ?"; // Use prepared statement parameter

        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerID); // Set the parameter value
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error setting started to 1: " + e.getMessage());
        }
    }

    public int getCurrentUnlocks(String cardName) {
        String sql = "SELECT currentUnlocks FROM prestigecards WHERE leftTitle = ?";
        int currentUnlocks = 0;

        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cardName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentUnlocks = rs.getInt("currentUnlocks");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching current unlocks for " + cardName + ": " + e.getMessage());
        }

        return currentUnlocks;
    }

    public int getPlayerTiles() {
        String sql = "SELECT playerTiles FROM userstats WHERE id = 1"; // Assuming 'id = 1' is the row you want to query
        int playerTiles = 0;
        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                playerTiles = rs.getInt("playerTiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching started: " + e.getMessage());
        }

        return playerTiles;
    }

    public void setPlayerTiles(int value, int player) {
        // Use the player parameter to target the specific row
        String selectSql = "SELECT playerTiles FROM userstats WHERE id = ?";
        String updateSql = "UPDATE userstats SET playerTiles = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Set the id for selecting
            selectStmt.setInt(1, player);

            // Fetch the current playerTiles
            int currentPlayerTiles = 0;
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    currentPlayerTiles = rs.getInt("playerTiles");
                }
            }

            // Increment playerTiles based on the passed value
            int newPlayerTiles = currentPlayerTiles + value;

            // Set parameters for the update statement
            updateStmt.setInt(1, newPlayerTiles);
            updateStmt.setInt(2, player); // Use the player parameter to target the specific row for update

            updateStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating playerTiles: " + e.getMessage());
        }
    }

    public int getRandomTiles() {
        String sql = "SELECT randomTiles FROM userstats WHERE id = 1"; // Assuming 'id = 1' is the row you want to query
        int randomTiles = 0;
        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                randomTiles = rs.getInt("randomTiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching started: " + e.getMessage());
        }

        return randomTiles;
    }

    public void setRandomTiles(int value) {
        String updateSql = "UPDATE userstats SET randomTiles = ? WHERE id IN (1, 2)";

        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            if (value == 0) {
                // Set bonusTiles to 0
                updateStmt.setInt(1, 0);
            } else if (value == 1) {
                // Increment bonusTiles
                int currentRandomTiles = getRandomTiles(); // Fetch the current bonusTiles value
                updateStmt.setInt(1, currentRandomTiles + 1);
            }
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating randomTiles: " + e.getMessage());
        }
    }

    public int getBonusTiles() {
        String sql = "SELECT bonusTiles FROM userstats WHERE id = 1"; // Assuming 'id = 1' is the row you want to query
        int bonusTiles = 0;
        try (Connection conn = getConnection(); // getConnection() should return your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                bonusTiles = rs.getInt("bonusTiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching started: " + e.getMessage());
        }

        return bonusTiles;
    }

    public void setBonusTiles(int value) {
        String updateSql = "UPDATE userstats SET bonusTiles = ? WHERE id IN (1, 2)";

        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            if (value == 0) {
                // Set bonusTiles to 0
                updateStmt.setInt(1, 0);
            } else {
                // Increment bonusTiles
                int currentBonusTiles = getBonusTiles();
                updateStmt.setInt(1, currentBonusTiles + value); // Increment by value
            }
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating bonusTiles: " + e.getMessage());
        }
    }
}