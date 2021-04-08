package com.nalexand.broker;

import com.nalexand.fx_utils.FXClient;
import com.nalexand.fx_utils.FXMessage;
import com.nalexand.fx_utils.FXMessageFactory;

import java.util.Scanner;

public class FXBroker {

    public static void main(String[] args) {
        Broker broker = new Broker();
        FXClient client = new FXClient(5001, "BROKER", broker);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String line = scanner.nextLine();
                String message = line.replace("|", "\u0001");
                client.sendMessage(FXMessageFactory.fromString(message));
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static class Broker implements FXClient.Listener {


        @Override
        public void onSuccess(FXMessage fxMessage) {

        }

        @Override
        public void onError(FXMessage fxMessage, Throwable e) {

        }
    }
}
