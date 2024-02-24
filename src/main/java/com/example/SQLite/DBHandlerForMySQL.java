package com.example.SQLite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBHandlerForMySQL {
    private static HikariDataSource dataSourceMySQL;
        static {
            HikariConfig configMySQLDB = new HikariConfig();
            configMySQLDB.setJdbcUrl("jdbc:mysql://localhost:3306/tileman");
            configMySQLDB.setUsername("root"); // Replace with your MySQL username
            configMySQLDB.setPassword("hgfdk46y56dfgoFGDste5tFgdeg346hGFD");
            //configMySQLDB.setMaximumPoolSize(10); // Adjust the pool size as needed
            dataSourceMySQL = new HikariDataSource(configMySQLDB);
        }

        public static Connection getConn() throws SQLException {
            return dataSourceMySQL.getConnection();
    }
}
