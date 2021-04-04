package com.nalexand.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketDelegate {

    public ServerSocket serverSocket;

    public Socket socket = null;

    public int port;

    private OutputStream outputStream = null;

    public SocketDelegate(int port) {
        this.port = port;
        serverSocket = openSocket(port);
    }

    public void sendMessage(byte[] message) {
        try {
            getOutputStream().write(message);
        } catch (IOException e) {
            FXRouter.handleError(e);
        }
    }

    public Thread createConnection(Consumer<byte[]> onMessageReceived) {
        Thread thread = new Thread(() -> {
            try {
                waitConnection();

                InputStream inputStream = socket.getInputStream();

                while (true) {
                    byte[] bytes = new byte[64];
                    int readCount = inputStream.read(bytes);

                    System.out.printf("%d: read %d bytes\n", port, readCount);
                    if (readCount > 0) {
                        onMessageReceived.accept(bytes);
                    }
                }
            } catch (IOException e) {
                FXRouter.handleError(e);
            }
        });
        thread.start();
        return thread;
    }

    public Socket waitConnection() {
        System.out.printf("ROUTER: %d: Wait receiver connection\n", port);
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            FXRouter.handleError(e);
        }
        System.out.printf("ROUTER: %d: Receiver connected\n", port);
        return socket;
    }

    public OutputStream getOutputStream() {
        try {
            if (outputStream == null) {
                outputStream = socket.getOutputStream();
            }
            return outputStream;
        } catch (IOException e) {
            FXRouter.handleError(e);
        }
        return null;
    }

    private ServerSocket openSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            FXRouter.handleError(e);
            return null;
        }
    }
}
