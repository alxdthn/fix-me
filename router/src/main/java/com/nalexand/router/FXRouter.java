package com.nalexand.router;

import com.nalexand.fx_utils.FXMessage;
import com.nalexand.fx_utils.FXServer;

import java.io.IOException;
import java.util.Scanner;

public class FXRouter {

    public static final boolean DEBUG = true;

    private static FXServer marketServer;

    private static FXServer brokerSever;

    public static void main(String[] args) {
        try {
            marketServer = new FXServer("MARKET", 5000, fxMessage -> onMessageReceived(5000, fxMessage));
            brokerSever = new FXServer("BROKER", 5001, fxMessage -> onMessageReceived(5001, fxMessage));
        } catch (IOException e) {
            handleError(e);
        }
        Scanner scanner = new Scanner(System.in);
        do {
            String line = scanner.nextLine();
            handleTerminalInput(line);
        } while (scanner.hasNext());
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

    private static void handleTerminalInput(String input) {
        switch (input) {
            case "status":
                brokerSever.printStatus();
                marketServer.printStatus();
                break;
            default:
                System.out.printf("FXServer: unknown command \"%s\"\n", input);
        }
    }
}
