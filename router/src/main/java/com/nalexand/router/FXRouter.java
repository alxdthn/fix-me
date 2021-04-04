package com.nalexand.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FXRouter {

    public static final boolean DEBUG = true;

    public static final SocketDelegate market = new SocketDelegate(5000);
    public static final SocketDelegate broker = new SocketDelegate(5001);

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        threads.add(createRouting(market, broker));
        threads.add(createRouting(broker, market));
        threads.forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        handleError(e);
                    }
                }
        );
    }

    private static Thread createRouting(
            SocketDelegate receiver,
            SocketDelegate dispatcher
    ) {
        Thread thread = new Thread(() -> {
            try {
                Socket receiverSocket = receiver.waitConnection();

                InputStream inputStream = receiverSocket.getInputStream();
                OutputStream outputStream = null;

                while (true) {
                    byte[] bytes = new byte[64];
                    int readCount = inputStream.read(bytes);
                    if (outputStream == null) {
                        outputStream = dispatcher.getOutputStream();
                    }
                    System.out.printf("%d: read %d bytes\n", receiverSocket.getLocalPort(), readCount);

                    outputStream.write(bytes);
                }
            } catch (IOException e) {
                handleError(e);
            }
        });
        thread.start();
        return thread;
    }

    public static void handleError(Exception e) {
        System.err.println("Something went wrong");
        if (DEBUG) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
