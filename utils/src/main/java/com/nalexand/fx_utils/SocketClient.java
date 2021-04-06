package com.nalexand.fx_utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SocketClient {

    public static final int CONNECTION_RETRY_TIMEOUT = 3000;

    private final int port;

    private String host;

    private final String logName;

    private Socket socket = null;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private int idCount = 0;

    private final Map<Integer, FXMessageHandler> messagePool = new Hashtable<>();

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
        Thread clientThread = new Thread(() -> {
            byte[] bytes = new byte[64];

            while (true) {
                tryCreateSocket();
                try {
                    InputStream inputStream = socket.getInputStream();
                    while (inputStream.read(bytes) > 0) {
                        FXMessage fxMessage = FXMessage.fromBytes(bytes);
                        logMessage(String.format("Answer is \"%s\"", fxMessage));
                        messagePool
                                .get(fxMessage.id)
                                .successHandler
                                .accept(fxMessage.message);
                        messagePool
                                .remove(fxMessage.id);
                    }
                    throw new IOException("No connection");
                } catch (IOException ignored) {
                    messagePool.clear();
                }
            }
        });
        clientThread.start();
    }

    public void sendMessage(
            String message,
            Consumer<String> successHandler,
            Consumer<Throwable> errorHandler
    ) {
        FXMessage fXMessage = new FXMessage(
                ++idCount,
                message
        );
        executorService.execute(
                () -> {
                    logMessage("Execute new message on thread: " + Thread.currentThread().getName());
                    try {
                        if (socket == null || !socket.isConnected()) {
                            logMessage("No connection");
                            return;
                        }

                        logMessage(String.format("Write message %s", message));
                        OutputStream outputStream = socket.getOutputStream();

                        outputStream.write(fXMessage.getBytes());
                        messagePool.put(fXMessage.id, new FXMessageHandler(successHandler, errorHandler));
                    } catch (IOException e) {
                        notifyErrorHandler(errorHandler, e);
                    }
                }
        );
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

    private void tryCreateSocket() {
        while (true) {
            try {
                socket = new Socket(host, port);
                logMessage(String.format("Connected %d port", port));
                return;
            } catch (IOException e) {
                logMessage(String.format("Retry connection to %d port", port));
                trySleep();
            }
        }
    }

    private void trySleep() {
        try {
            Thread.sleep(CONNECTION_RETRY_TIMEOUT);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
            System.exit(1);
        }
    }

    private static class FXMessageHandler {

        Consumer<String> successHandler;

        Consumer<Throwable> errorHandler;

        public FXMessageHandler(Consumer<String> successHandler, Consumer<Throwable> errorHandler) {
            this.successHandler = successHandler;
            this.errorHandler = errorHandler;
        }
    }
}
