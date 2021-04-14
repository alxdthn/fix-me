package com.nalexand.fx_utils.message;

import java.util.HashMap;
import java.util.Map;

public class FXMessageField {

    public static final Key BEGIN_STRING = new Key("8", "BeginString");
    public static final Key BODY_LENGTH = new Key("9", "BodyLength");
    public static final Key CHECK_SUM = new Key("10", "CheckSum");
    public static final Key MSG_TYPE = new Key("35", "MsgType");
    public static final Key ORDER_QTY = new Key("38", "OrderQty");
    public static final Key ORDER_STATUS = new Key("38", "OrderStatus");
    public static final Key PRICE = new Key("44", "Price");
    public static final Key SENDER_ID = new Key("49", "SenderId");
    public static final Key SEND_TIME = new Key("52", "SendTime");
    public static final Key SIDE = new Key("54", "Side");
    public static final Key TICKER = new Key("55", "Ticker");
    public static final Key TARGET_ID = new Key("56", "TargetId");

    private static final Map<String, Key> collection = createCollection();

    Key key;
    String value;

    FXMessageField(Key key) {
        this.key = key;
    }

    String fixFormat() {
        return String.format("%s=%s", key.key, value);
    }

    String userFormat() {
        return String.format("%s(%s): %s", key.name, key.key, value);
    }

    public static Key from(String key) {
        return collection.get(key);
    }

    private static Map<String, Key> createCollection() {
        Map<String, Key> collection = new HashMap<>();
        collection.put(BEGIN_STRING.key, BEGIN_STRING);
        collection.put(BODY_LENGTH.key, BODY_LENGTH);
        collection.put(CHECK_SUM.key, CHECK_SUM);
        collection.put(MSG_TYPE.key, MSG_TYPE);
        collection.put(ORDER_QTY.key, ORDER_QTY);
        collection.put(ORDER_STATUS.key, ORDER_STATUS);
        collection.put(PRICE.key, PRICE);
        collection.put(SENDER_ID.key, SENDER_ID);
        collection.put(SEND_TIME.key, SEND_TIME);
        collection.put(SIDE.key, SIDE);
        collection.put(TICKER.key, TICKER);
        collection.put(TARGET_ID.key, TARGET_ID);
        return collection;
    }

    public static class Key {

        String key;
        String name;

        Key(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }
}