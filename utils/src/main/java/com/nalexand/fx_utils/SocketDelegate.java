package com.nalexand.fx_utils;

import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
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
            socket.getOutputStream()
                    .write((fxMessage.toFixString() + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            while (scanner.hasNext()) {
                byte[] nextBytes = scanner.nextLine().getBytes();
                FXMessage fxMessage = FXMessageFactory
                        .fromBytes(nextBytes);
                System.out.printf("Read %d bytes\n", nextBytes.length);
                onMessageReceived.accept(fxMessage);
            }
            throw new IOException("Connection closed");
        } catch (IOException e) {
            System.out.println("Drop connection");
            onDisconnected.run();
        }
    }
}
