package com.nalexand.fx_utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketClient {

    private Socket socket;

    private final String logName;

    private OutputStream outputStream = null;

    public SocketClient(String host, int port, String logName) {
        this.logName = logName;
        try {
            socket = new Socket(host, port);
            logMessage(String.format("Connected to %d port", port));

            new Thread(this::readInput).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void sendMessage(String message) {
        if (message == null) return;
        try {
            if (outputStream == null) {
                outputStream = socket.getOutputStream();
            }
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void readInput() {
        try {
            byte[] bytes = new byte[64];
            InputStream inputStream = socket.getInputStream();

            while (true) {
                int readCount = inputStream.read(bytes);
                System.out.println(new String(bytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void logMessage(String message) {
        if (logName != null) {
            System.out.printf("%s: %s\n", logName, message);
        }
    }
}
