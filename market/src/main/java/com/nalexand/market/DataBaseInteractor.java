package com.nalexand.market;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DataBaseInteractor {

    private Connection connection = null;

    private Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:market.db");
                String sql = "CREATE TABLE IF NOT EXISTS TRANSACTION_HISTORY (\n"
                        + "id integer PRIMARY KEY,\n"
                        + "quantity integer,\n"
                        + "price decimal\n"
                        + ");";
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
            } catch (Exception e) {

            }
        }
        return connection;
    }
}