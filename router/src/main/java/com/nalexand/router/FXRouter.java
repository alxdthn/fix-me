package com.nalexand.router;

import com.nalexand.fx_utils.FXMessage;

public class FXRouter {

    public static final boolean DEBUG = true;

    public static void main(String[] args) {
        FXServer marketServer = new FXServer(5000, fxMessage -> onMessageReceived(5000, fxMessage));
        FXServer brokerSever = new FXServer(5001, fxMessage -> onMessageReceived(5001, fxMessage));
    }

    public static void handleError(Exception e) {
        System.err.println("Something went wrong");
        if (DEBUG) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public static void onMessageReceived(int port, FXMessage fxMessage) {
        System.out.format("FXRouter: Received message %d: %s\n", port, fxMessage);
    }
}
