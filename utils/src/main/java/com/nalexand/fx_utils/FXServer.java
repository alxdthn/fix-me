package com.nalexand.fx_utils;

import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FXServer implements Runnable {

    public static final String SERVER_SENDER_ID = "SERVER";

    public final String name;

    private static int id = 100000;

    private final Consumer<FXMessage> onMessageReceived;

    private final Map<String, SocketDelegate> connectedSockets = new HashMap<>();

    private final int port;

    private final ServerSocket serverSocket;

    public FXServer(String name, int port, Consumer<FXMessage> onMessageReceived) throws IOException {
        this.name = name;
        this.port = port;
        this.onMessageReceived = onMessageReceived;
        this.serverSocket = new ServerSocket(port);
        logMessage("Created");
        Executors.defaultThreadFactory().newThread(this).start();
    }

    public boolean routeMessage(String id, FXMessage fxMessage) {
        SocketDelegate socketDelegate = connectedSockets.get(id);
        if (socketDelegate == null) return false;
        socketDelegate.sendMessage(fxMessage);
        return true;
    }

    public void sendMessage(String id, FXMessage fxMessage) {
        fxMessage.body.setTargetId(id);
        fxMessage.prepare(SERVER_SENDER_ID);
        connectedSockets.get(id).sendMessage(fxMessage);
    }

    public void printStatus() {
        logMessage("Status: %d connections", connectedSockets.size());
        if (connectedSockets.size() != 0) {
            System.out.printf("%-8s|%-8s\n", "id", "status");
            connectedSockets.forEach((id, socketDelegate) -> {
                System.out.printf("%-8s|%-8s\n", id, socketDelegate.isConnected());
            });
        }
        System.out.println();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                String newId = Integer.toString(id++);

                SocketDelegate socketDelegate = new SocketDelegate(socket, onMessageReceived, () -> {
                    connectedSockets.remove(newId);
                });

                connectedSockets.put(newId, socketDelegate);
                FXMessage logonMessage = FXMessageFactory.createLogon();
                logonMessage.prepare(newId);
                routeMessage(newId, logonMessage);
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
