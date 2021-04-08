package com.nalexand.fx_utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FXClient {

    public static final int CONNECTION_RETRY_TIMEOUT = 3000;

    private final int port;

    private String host;

    private final String logName;

    private Socket socket = null;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Map<String, FXMessage> messagePool = new Hashtable<>();

    private final Listener listener;

    public FXClient(int port, String logName, Listener listener) {
        this.logName = logName;
        this.port = port;
        this.listener = listener;
        try {
            this.host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logMessage(String.format("Create client %s:%d", host, port));
        Thread clientThread = new Thread(() -> {
            byte[] bytes = new byte[64];

            while (true) {
                tryCreateSocket();
                try {
                    InputStream inputStream = socket.getInputStream();
                    while (inputStream.read(bytes) > 0) {
                        FXMessage fxMessage = FXMessage.fromBytes(bytes);
                        logMessage(String.format("Answer is:\n%s", fxMessage));
                        listener.onSuccess(fxMessage);
                        messagePool.remove(fxMessage.body.messageNum);
                    }
                    throw new IOException("No connection");
                } catch (IOException ignored) {
                    messagePool.clear();
                }
            }
        });
        clientThread.start();
    }

    public void sendMessage(FXMessage fxMessage) {
        executorService.execute(
                () -> {
                    logMessage("Execute new message on thread: " + Thread.currentThread().getName());
                    messagePool.put(fxMessage.body.messageNum, fxMessage);

                    try {
                        if (socket == null || !socket.isConnected()) {
                            throw new IOException("No connection");
                        }

                        logMessage(String.format("Write message %s", fxMessage));
                        OutputStream outputStream = socket.getOutputStream();

                        outputStream.write(fxMessage.getBytes());
                    } catch (IOException e) {
                        messagePool.remove(fxMessage.body.messageNum);
                        notifyErrorHandler(fxMessage, e);
                    }
                }
        );
    }

    private void notifyErrorHandler(FXMessage fxMessage, Throwable error) {
        error.printStackTrace();
        logMessage(String.format("Error: %s", error.getMessage()));
        listener.onError(fxMessage, error);
    }

    private void logMessage(String message) {
        if (logName != null) {
            System.out.printf("%s: %s\n", logName, message);
        }
    }

    private void tryCreateSocket() {
        while (true) {
            try {
                socket = new Socket(host, port);
                logMessage(String.format("Connected %d port", port));
                return;
            } catch (IOException e) {
                logMessage(String.format("Retry connection to %d port", port));
                trySleep();
            }
        }
    }

    private void trySleep() {
        try {
            Thread.sleep(CONNECTION_RETRY_TIMEOUT);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
            System.exit(1);
        }
    }

    public interface Listener {

        void onSuccess(FXMessage fxMessage);
        void onError(FXMessage fxMessage, Throwable e);
    }
}
