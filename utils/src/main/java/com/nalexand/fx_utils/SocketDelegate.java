package com.nalexand.fx_utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SocketDelegate implements Runnable {

    private final Socket socket;

    private final Consumer<FXMessage> onMessageReceived;

    private final Runnable onDisconnected;

    public SocketDelegate(Socket socket, Consumer<FXMessage> onMessageReceived, Runnable onDisconnected) {
        this.socket = socket;
        this.onMessageReceived = onMessageReceived;
        this.onDisconnected = onDisconnected;

        Executors
                .defaultThreadFactory()
                .newThread(this)
                .start();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void sendMessage(FXMessage fxMessage) {
        try {
            socket.getOutputStream().write(fxMessage.getBytes());
        } catch (IOException ignored) {

        }
    }

    @Override
    public void run() {
        byte[] bytes = new byte[Utils.READ_BUFF_SIZE];

        try {
            InputStream inputStream = socket.getInputStream();
            int readCount;
            while ((readCount = inputStream.read(bytes)) > 0) {
                System.out.printf("Read %d bytes\n", readCount);
                onMessageReceived.accept(FXMessageFactory.fromBytes(bytes));
            }
            throw new IOException("Connection closed");
        } catch (IOException e) {
            System.out.println("Drop connection");
            onDisconnected.run();
        }
    }
}
