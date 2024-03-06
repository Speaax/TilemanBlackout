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

            // Create UserStats table
            String sql = "CREATE TABLE IF NOT EXISTS userstats (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
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

    public void updateUserStats(int playerTiles, int randomTiles, int bonusTiles, int playerId) {
        String sql = "UPDATE userstats SET playerTiles = ?, randomTiles = ?, bonusTiles = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerTiles);
            pstmt.setInt(2, randomTiles);
            pstmt.setInt(3, bonusTiles);
            pstmt.setInt(4, playerId);

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
                    new UserStatsData(0, 0, 0, 0), // Adjust values as needed
                    new UserStatsData(0, 0, 0, 0) // Adjust values as needed
            );
            for (UserStatsData userStats : initialUserStats) {
                insertUserStats(userStats.getPlayerTiles(), userStats.getRandomTiles(), userStats.getBonusTiles(), userStats.getStarted());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertUserStats (int playerTiles, int randomTiles, int bonusTiles, int started){

        String sql = "INSERT INTO userstats(playerTiles, randomTiles, bonusTiles, started) VALUES(?,?,?,?)";

        try (Connection conn = getConnection(); // Get a connection from the connection pool
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playerTiles);
            pstmt.setInt(2, randomTiles);
            pstmt.setInt(3, bonusTiles);
            pstmt.setInt(4, started);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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

    public int getNumberOfTiles() {
        String sql = "SELECT COUNT(*) AS total_tiles FROM tiles;";
        int totalTiles = 0;
        try (Connection conn = getConnection(); // Assuming getConnection() provides your database connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalTiles = rs.getInt("total_tiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching number of tiles: " + e.getMessage());
        }

        return totalTiles;
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

    public int getPlayerTiles(int id) {
        String sql = "SELECT playerTiles FROM userstats WHERE id = ?"; // Use a placeholder '?'
        int playerTiles = 0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the placeholder value with the actual id
            pstmt.setInt(1, id); // '1' here refers to the first placeholder in the query

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                playerTiles = rs.getInt("playerTiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching playerTiles: " + e.getMessage());
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

    public int getRandomTiles(int id) {
        String sql = "SELECT randomTiles FROM userstats WHERE id = ?"; // Use a placeholder '?'
        int randomTiles = 0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the placeholder value with the actual id
            pstmt.setInt(1, id); // '1' here refers to the first placeholder in the query

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                randomTiles = rs.getInt("randomTiles");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching randomTiles: " + e.getMessage());
        }

        return randomTiles;
    }

    public void setRandomTiles(int id) {
        String sql = "UPDATE userstats SET randomTiles = randomTiles + 1 WHERE id = ?"; // Use placeholders for value and id

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the placeholder value for user ID
            pstmt.setInt(1, id); // User ID

            pstmt.executeUpdate(); // Execute the update statement
        } catch (SQLException e) {
            System.out.println("Error setting randomTiles: " + e.getMessage());
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