package com.nalexand.market;

import com.nalexand.fx_utils.message.FXMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DataBaseInteractor {

    private Connection connection = null;

    public String assignedId;

    public void saveTransaction(FXMessage fxMessage) throws DatabaseInteractionException {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement
                    ("INSERT INTO 'transaction_history' ('market_id', 'transaction') VALUES(?,?)");
            preparedStatement.setInt(1, Integer.parseInt(assignedId));
            preparedStatement.setString(2, fxMessage.toFixString());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new DatabaseInteractionException(e);
        }
    }

    private Connection getConnection() throws DatabaseInteractionException {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:market.db");
                String sql = "CREATE TABLE IF NOT EXISTS 'transaction_history' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'market_id' INTEGER, 'transaction' TEXT);";
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
            } catch (Exception e) {
                throw new DatabaseInteractionException(e);
            }
        }
        return connection;
    }

    public static class DatabaseInteractionException extends RuntimeException {

        public DatabaseInteractionException(Throwable cause) {
            super(cause);
        }
    }
}