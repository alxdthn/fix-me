package com.nalexand.broker;

import com.nalexand.fx_utils.SocketClient;

import java.util.Scanner;

public class FXBroker {

    public static void main(String[] args) {
        SocketClient client = new SocketClient("macbook-atalkhin.local", 5001, "BROKER");
        client.setOnMessageReceivedListener(bytes -> System.out.println(new String(bytes)));

        Scanner scanner = new Scanner(System.in);
        String line = null;
        do {
            try {
                line = scanner.nextLine();
                client.sendMessage(line);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } while (line != null);
    }
}
