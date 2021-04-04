package com.nalexand.market;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class FXMarket {

    public static final boolean DEBUG = true;

    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("il-b4.msk.21-school.ru", 5000);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line != null) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(line.getBytes());
                line = scanner.nextLine();
            } catch (IOException e) {

            }
        }
    }
}
