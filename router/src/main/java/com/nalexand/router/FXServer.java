package com.nalexand.router;

import com.nalexand.fx_utils.FXMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FXServer {

    private int id = 100000;

    private final Map<Integer, SocketDelegate> connectedSockets = new HashMap<>();

    public FXServer(int port, Consumer<FXMessage> onMessageReceived) {
        new Thread(
                () -> {
                    while (true) {
                        try {
                            connectedSockets.put(
                                    ++id,
                                    new SocketDelegate(port, onMessageReceived)
                            );
                        } catch (IOException ignored) {

                        }
                    }
                }
        ).start();
    }
}
