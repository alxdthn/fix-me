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
                    ("SAVE INTO TRANSACTION_HISTORY (date, transaction) VALUES(?, ?)");
            preparedStatement.setString(1, fxMessage.header.getSendTime());
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
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:market_%s.db", assignedId));
                String sql = "CREATE TABLE IF NOT EXISTS TRANSACTION_HISTORY (\n"
                        + "date string PRIMARY KEY,\n"
                        + "transaction string\n"
                        + ");";
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