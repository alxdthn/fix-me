package com.nalexand.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FXRouter {

    public static final boolean DEBUG = true;

    public static void main(String[] args) {
        ServerSocket serverSocket = openSocket(5000);
        ServerSocket marketSocket = openSocket(5001);

        List<Thread> threads = new ArrayList<>();
        threads.add(startSocket(serverSocket));
        threads.add(startSocket(marketSocket));
        threads.forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        handleError(e);
                    }
                }
        );
    }

    private static ServerSocket openSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            handleError(e);
            return null;
        }
    }

    private static Thread startSocket(final ServerSocket serverSocket) {
        Thread socketThread = new Thread(
                () -> {
                    try {
                        System.out.printf("%d: Connection established\n", serverSocket.getLocalPort());
                        Socket socket = serverSocket.accept();
                        System.out.printf("%d: Someone connected\n", serverSocket.getLocalPort());
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();

                        while (true) {
                            byte[] bytes = new byte[64];
                            int readCount = inputStream.read(bytes);
                            System.out.printf("%d: read %d bytes\n", serverSocket.getLocalPort(), readCount);

                            outputStream.write(bytes);
                        }
                    } catch (IOException e) {
                        handleError(e);
                    }
                }
        );
        socketThread.start();
        return socketThread;
    }

    private static void handleError(Exception e) {
        System.err.println("Something went wrong");
        if (DEBUG) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
