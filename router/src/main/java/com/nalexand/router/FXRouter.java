package com.nalexand.router;

import java.util.ArrayList;
import java.util.List;

public class FXRouter {

    public static final boolean DEBUG = true;

    public static final SocketDelegate market = new SocketDelegate(5000);
    public static final SocketDelegate broker = new SocketDelegate(5001);

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        threads.add(market.createConnection(broker::sendMessage));
        threads.add(broker.createConnection(market::sendMessage));
        threads.forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        handleError(e);
                    }
                }
        );
    }

    public static void handleError(Exception e) {
        System.err.println("Something went wrong");
        if (DEBUG) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
