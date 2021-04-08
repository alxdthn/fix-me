package com.nalexand.fx_utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FXServer implements Runnable {

    private final Consumer<FXMessage> onMessageReceived;

    private final Map<Integer, SocketDelegate> connectedSockets = new HashMap<>();

    private final String name;

    private final int port;

    private int id = 100000;

    private ServerSocket serverSocket;

    public FXServer(String name, int port, Consumer<FXMessage> onMessageReceived) throws IOException {
        this.name = name;
        this.port = port;
        this.onMessageReceived = onMessageReceived;
        this.serverSocket = new ServerSocket(port);
        logMessage("Created");
        Executors.defaultThreadFactory().newThread(this).start();
    }

    public void sendMessage(int id, FXMessage fxMessage) {
        connectedSockets.get(id).sendMessage(fxMessage);
    }

    public void printStatus() {
        logMessage("Status: %d connections", connectedSockets.size());
        if (connectedSockets.size() != 0) {
            System.out.printf("%-8s|%-8s\n", "id", "status");
            connectedSockets.forEach((id, socketDelegate) -> {
                System.out.printf("%-8d|%-8s\n", id, socketDelegate.isConnected());
            });
        }
        System.out.println();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                int newId = id++;

                SocketDelegate socketDelegate = new SocketDelegate(socket, onMessageReceived, () -> {
                    connectedSockets.remove(newId);
                });

                connectedSockets.put(newId, socketDelegate);
                logMessage("Socket started");
            } catch (IOException ignored) {

            }
        }
    }

    private void logMessage(String format, Object... args) {
        Object[] nextArgs = Arrays.stream(args).toArray();
        System.out.printf("FXServer(%s) %d: %s\n", name, port, String.format(format, nextArgs));
    }
}
