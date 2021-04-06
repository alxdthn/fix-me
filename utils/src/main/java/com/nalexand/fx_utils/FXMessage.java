package com.nalexand.fx_utils;

public class FXMessage {

    int id;

    String message;

    public FXMessage(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }

    public static FXMessage fromBytes(byte[] bytes) {
        String rawMessage = new String(bytes);
        String[] split = rawMessage.split("\\|");
        String messageData = split[0].trim();
        String messageChecksum = split[split.length - 1].trim();
        return new FXMessage(
                Integer.parseInt(messageChecksum),
                messageData
        );
    }

    public static FXMessage createAnswerFromBytes(byte[] bytes, String answer) {
        return new FXMessage(
                fromBytes(bytes).id,
                answer
        );
    }

    @Override
    public String toString() {
        return String.format("%s | %d", message, id);
    }
}
