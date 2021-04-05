package com.nalexand.market;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.nalexand.fx_utils.SocketClient;

public class FXMarket {

    public static void main(String[] args) {
        SocketClient client = new SocketClient(5000, "MARKET");

        Scanner scanner = new Scanner(System.in);
        String line = null;
        do {
            try {
                line = scanner.nextLine();
                client.sendMessage(
                        line,
                        result -> {
                            System.out.println(result);
                        },
                        error -> {
                            System.out.println(error);
                        }
                );
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } while (line != null);
    }
}
