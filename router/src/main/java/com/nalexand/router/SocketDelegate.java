package com.nalexand.router;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketDelegate {

    public ServerSocket serverSocket;

    public Socket socket = null;

    public int port;

    public SocketDelegate(int port) {
        this.port = port;
        serverSocket = openSocket(port);
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
            return socket.getOutputStream();
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
