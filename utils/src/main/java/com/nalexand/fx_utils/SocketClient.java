package com.nalexand.fx_utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {

    public static final int CONNECTION_RETRY_TIMEOUT = 3000;

    private int port;

    private String host;

    private Socket socket;

    private final String logName;

    private OutputStream outputStream = null;

    private Consumer<byte[]> onMessageReceived = null;

    private Thread socketThread = null;

    public SocketClient(String host, int port, String logName) {
        this.logName = logName;
        this.port = port;
        this.host = host;
        Thread clientThread = new Thread(() -> {
            logMessage(String.format("Create client %s:%d", host, port));
            while (true) {
                if (socketThread != null) {
                    socketThread.interrupt();
                }
                tryCreateSocket();
                runSocketThread();
            }
        });
        clientThread.start();
    }

    private void tryCreateSocket() {
        while (true) {
            try {
                socket = new Socket(host, port);
                return;
            } catch (IOException e) {
                logMessage(String.format("Retry connection to %d port", port));
                trySleep();
            }
        }
    }

    public void sendMessage(String message) {
        if (message == null) return;
        try {
            if (outputStream == null) {
                outputStream = socket.getOutputStream();
            }
            outputStream.write(message.getBytes());
        } catch (IOException ignored) { }
    }

    private void readInput() {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[64];

            while (inputStream.read(bytes) > 0) {
                onMessageReceived.accept(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runSocketThread() {
        socketThread = new Thread(() -> {
            logMessage(String.format("Connected to %d port", port));
            readInput();
        });
        socketThread.start();
        try {
            socketThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logMessage(String message) {
        if (logName != null) {
            System.out.printf("%s: %s\n", logName, message);
        }
    }

    public void setOnMessageReceivedListener(Consumer<byte[]> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    private void trySleep() {
        try {
            Thread.sleep(CONNECTION_RETRY_TIMEOUT);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
            System.exit(1);
        }
    }
}
