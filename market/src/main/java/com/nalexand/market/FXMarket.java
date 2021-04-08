package com.nalexand.market;

import com.nalexand.fx_utils.FXClient;
import com.nalexand.fx_utils.FXMessage;

import java.util.Scanner;

public class FXMarket {

    public static void main(String[] args) {
        Market market = new Market();
        FXClient client = new FXClient(5000, "MARKET", market);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String line = scanner.nextLine();
                client.sendMessage(FXMessage.fromBytes(line.getBytes()));
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static class Market implements FXClient.Listener {

        @Override
        public void onSuccess(FXMessage fxMessage) {

        }

        @Override
        public void onError(FXMessage fxMessage, Throwable e) {

        }
    }
}
