package com.nalexand.broker;

import com.nalexand.fx_utils.FXClient;
import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;

import java.util.Scanner;

public class FXBroker {

    public static void main(String[] args) {
        Broker broker = new Broker();
        FXClient client = new FXClient(5001, "BROKER", broker);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String line = scanner.nextLine();
                if (line.startsWith("8=" + FXMessage.PROTOCOL_VERSION)) {
                    String message = line.replace("|", "\u0001");
                    client.sendMessage(
                            FXMessageFactory.fromString(message)
                    );
                } else {
                    try {
                        client.sendMessage(
                                FXMessageFactory.fromInput(line)
                        );
                    } catch (FXClient.FXBadMessageException e) {
                        client.logMessage(e.getMessage());
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static class Broker implements FXClient.Listener {


        @Override
        public void onMessageReceived(FXMessage fxMessage) {

        }

        @Override
        public void onMessageSendError(FXMessage fxMessage, Throwable e) {

        }
    }
}
