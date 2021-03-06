package com.nalexand.router;

import com.nalexand.fx_utils.FXServer;
import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;

import java.io.IOException;
import java.util.Scanner;

public class FXRouter {

    public static final boolean DEBUG = true;

    private static FXServer marketServer;

    private static FXServer brokerSever;

    public static void main(String[] args) {
        try {
            marketServer = new FXServer("MARKET", 5000, FXRouter::onMessageReceivedFromMarket);
            brokerSever = new FXServer("BROKER", 5001, FXRouter::onMessageReceivedFromBroker);
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

    public static void onMessageReceivedFromBroker(FXMessage fxMessage) {
        System.out.format("FXRouter: Received message from broker\nmsg: %s\n", fxMessage);
        routeBetween(marketServer, brokerSever, fxMessage);
    }

    public static void onMessageReceivedFromMarket(FXMessage fxMessage) {
        System.out.format("FXRouter: Received message from market\nmsg: %s\n", fxMessage);
        routeBetween(brokerSever, marketServer, fxMessage);
    }

    private static void routeBetween(FXServer target, FXServer sender, FXMessage fxMessage) {
        boolean isMessageSent = target.routeMessage(
                fxMessage.header.getTargetId(),
                fxMessage
        );
        if (!isMessageSent) {
            FXMessage rejectMessage = FXMessageFactory.createRejected(
                    fxMessage
            );
            sender.sendMessage(fxMessage.header.getSenderId(), rejectMessage);
        }
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
