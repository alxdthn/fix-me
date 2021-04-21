package com.nalexand.fx_utils;

import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FXClient {

    public static final int CONNECTION_RETRY_TIMEOUT = 3000;

    private final int port;

    private String host;

    private final String logName;

    private Socket socket = null;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Listener listener;

    private String assignedId = null;

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
        Thread clientThread = new Thread(this::readMessages);
        clientThread.start();
    }

    public void sendMessage(FXMessage fxMessage) {
        if (fxMessage.error != null) {
            logMessage("Can't send message: %s", fxMessage.error);
        }
        executorService.execute(
                () -> {
                    logMessage("Execute new message on thread: " + Thread.currentThread().getName());

                    try {
                        if (socket == null || !socket.isConnected()) {
                            throw new IOException("No connection");
                        }

                        OutputStream outputStream = socket.getOutputStream();

                        fxMessage.prepare(assignedId);
                        logMessage(String.format("Send message:\n%s", fxMessage));
                        outputStream.write(fxMessage.getBytes());
                    } catch (IOException e) {
                        notifyErrorHandler(fxMessage, e);
                    }
                }
        );
    }

    private void readMessages() {
        byte[] bytes = new byte[Utils.READ_BUFF_SIZE];

        while (true) {
            tryCreateSocket();
            try {
                InputStream inputStream = socket.getInputStream();
                while (inputStream.read(bytes) > 0) {
                    FXMessage fxMessage = FXMessageFactory.fromBytes(bytes);
                    logMessage(String.format("Received message:\n%s", fxMessage));
                    if (fxMessage.error != null) {
                        listener.onMessageSendError(fxMessage, new FXBadMessageException(fxMessage.error));
                    } else {
                        readMessage(fxMessage);
                        listener.onMessageReceived(fxMessage);
                    }
                }
                throw new IOException("No connection");
            } catch (IOException ignored) {
                assignedId = null;
            }
        }
    }

    private void readMessage(FXMessage fxMessage) throws FXBadMessageException {
        try {
            if (FXMessage.MSG_TYPE_LOGON.equals(fxMessage.header.getMsgType())) {
                assignedId = fxMessage.header.getTargetId();
                logMessage("Assigned id: %s", assignedId);
            }
        } catch (NullPointerException e) {
            throw new FXBadMessageException(String.format("Bad message:\n%s", fxMessage));
        }
    }

    private void notifyErrorHandler(FXMessage fxMessage, Throwable error) {
        error.printStackTrace();
        logMessage(String.format("Error: %s", error.getMessage()));
        listener.onMessageSendError(fxMessage, error);
    }

    public void logMessage(String message, Object... args) {

        if (logName != null) {
            Object[] nextArgs = Arrays.stream(args).toArray();
            System.out.printf("%s: %s\n", logName, String.format(message, nextArgs));
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

    public static class FXBadMessageException extends IOException {

        public FXBadMessageException(String message) { super(message); }
    }

    public interface Listener {

        void onMessageReceived(FXMessage fxMessage);
        void onMessageSendError(FXMessage fxMessage, Throwable e);
    }
}
