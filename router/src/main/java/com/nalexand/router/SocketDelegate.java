package com.nalexand.router;

import com.nalexand.fx_utils.FXMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SocketDelegate {

    public ServerSocket serverSocket;

    public Socket socket = null;

    public int port;

    private OutputStream outputStream = null;

    public SocketDelegate(int port, Consumer<FXMessage> messageReceived) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        createConnection(messageReceived);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void sendMessage(byte[] message) {
        try {
            getOutputStream().write(message);
        } catch (IOException e) {
        }
    }

    public void createConnection(Consumer<FXMessage> onMessageReceived) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    waitConnection();

                    InputStream inputStream = socket.getInputStream();

                    byte[] bytes = new byte[64];
                    int readCount;
                    while ((readCount = inputStream.read(bytes)) > 0) {
                        System.out.printf("%d: Read %d bytes\n", port, readCount);
                        onMessageReceived.accept(FXMessage.fromBytes(bytes));
                    }
                    System.out.printf("%d: Drop connection\n", port);
                } catch (IOException e) {
                    FXRouter.handleError(e);
                }
            }
        });
        thread.start();
    }

    public void waitConnection() {
        System.out.printf("ROUTER: %d: Wait receiver connection\n", port);
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            FXRouter.handleError(e);
        }
        System.out.printf("ROUTER: %d: Receiver connected\n", port);
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
}
