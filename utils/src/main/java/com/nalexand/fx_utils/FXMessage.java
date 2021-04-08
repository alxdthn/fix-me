package com.nalexand.fx_utils;

import static com.nalexand.fx_utils.CommonUtils.listOfNotNull;
import static com.nalexand.fx_utils.Utils.field;

public class FXMessage {

    public static final String DELIMITER = "\u0001";
    public static final String PROTOCOL_VERSION = "FIX.4.4";
    public static final String TRANSACTION_TYPE_BUY = "1";
    public static final String TRANSACTION_TYPE_SELL = "?"; //  TODO
    public static final String MSG_TYPE_NEW_REQUEST = "D";
    public static final String MSG_TYPE_LOGON = "A";

    public String error = null;
    public Header header = null;
    public Body body = null;

    FXMessage(String error) {
        this.error = error;
    }

    FXMessage(Header header, Body body) {
        this.header = header;
        this.body = body;
    }

    @Override
    public String toString() {
        if (error != null) {
            return error;
        } else {
            return header.toString() + DELIMITER + body.toString(true);
        }
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }

    public static class FieldKey {

        private FieldKey() {
        }

        public static final int BEGIN_STRING = 8;
        public static final int BODY_LENGTH = 9;
        public static final int SUM = 10;
        public static final int MSG_NUM = 34;
        public static final int MSG_TYPE = 35;
        public static final int QUANTITY = 38;
        public static final int PRICE = 44;
        public static final int SENDER_ID = 49;
        public static final int SEND_TIME = 52;
        public static final int TRANSACTION_TYPE = 54;
        public static final int TICKER = 55;
        public static final int MARKET = 56;
    }

    public static class Header {
        int bodyLength;
        String beginString;

        public Header() {}

        public Header(Body body) {
            this.beginString = PROTOCOL_VERSION;
            this.bodyLength = body.toString(false).length() + 1;
        }

        @Override
        public String toString() {
            return String.join(DELIMITER, new String[] {
                    field(FieldKey.BEGIN_STRING, beginString),
                    field(FieldKey.BODY_LENGTH, Integer.toString(bodyLength))
            });
        }
    }

    public static class Body {
        String assignedId;
        String messageNum;
        String messageType;
        String transactionType;
        String sendTime;
        String market;
        String ticker;
        String quantity;
        String price;
        String sum;

        public Body() {
        }

        public Body(String assignedId, String messageNum, String messageType, String sendTime, String transactionType, String market, String ticker, String quantity, String price) {
            this.assignedId = assignedId;
            this.messageNum = messageNum;
            this.messageType = messageType;
            this.transactionType = transactionType;
            this.sendTime = sendTime;
            this.market = market;
            this.ticker = ticker;
            this.quantity = quantity;
            this.price = price;
        }

        public String toString(boolean withSum) {
            return String.join(DELIMITER, listOfNotNull(
                    field(FieldKey.MSG_TYPE, messageType),
                    field(FieldKey.MSG_NUM, messageNum),
                    field(FieldKey.SENDER_ID, assignedId),
                    field(FieldKey.MARKET, market),
                    field(FieldKey.SEND_TIME, sendTime),
                    field(FieldKey.TRANSACTION_TYPE, transactionType),
                    field(FieldKey.TICKER, ticker),
                    field(FieldKey.QUANTITY, quantity),
                    field(FieldKey.PRICE, price),
                    ((withSum) ? field(FieldKey.SUM, sum) : null)
            ));
        }
    }
}
