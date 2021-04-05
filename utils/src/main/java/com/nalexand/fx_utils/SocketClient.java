package com.nalexand.fx_utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SocketClient {

    private final int port;

    private String host;

    private final String logName;

    private Socket socket = null;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public SocketClient(int port, String logName) {
        this.logName = logName;
        this.port = port;
        try {
            this.host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        logMessage(String.format("Create client %s:%d", host, port));
    }

    public void sendMessage(
            String message,
            Consumer<String> resultHandler,
            Consumer<Throwable> errorHandler
    ) {
        executorService.execute(
                () -> {
                    logMessage("Execute new message on thread: " + Thread.currentThread().getName());
                    try {
                        if (socket == null || !socket.isConnected()) {
                            logMessage(String.format("Create connection with port %d", port));
                            socket = new Socket(host, port);
                        }

                        logMessage(String.format("Write message %s", message));
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(message.getBytes());

                        logMessage("Wait answer");
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[64];
                        if (inputStream.read(bytes) < 0) {
                            throw new IOException("No connection");
                        } else {
                            String answer = new String(bytes);
                            logMessage(String.format("Answer is %s", answer));
                            resultHandler.accept(answer);
                        }
                    } catch (IOException e) {
                        closeConnection();
                        notifyErrorHandler(errorHandler, e);
                    }
                }
        );
    }

    private void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        socket = null;
    }

    private void notifyErrorHandler(Consumer<Throwable> errorHandler, Throwable error) {
        error.printStackTrace();
        logMessage(String.format("Error: %s", error.getMessage()));
        if (errorHandler != null) {
            errorHandler.accept(error);
        }
    }

    private void logMessage(String message) {
        if (logName != null) {
            System.out.printf("%s: %s\n", logName, message);
        }
    }
}
