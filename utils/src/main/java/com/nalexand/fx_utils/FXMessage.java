package com.nalexand.fx_utils;

import static com.nalexand.fx_utils.CommonUtils.listOfNotNull;
import static com.nalexand.fx_utils.Utils.field;

public class FXMessage {

    public static final String DELIMITER = "\u0001";
    public static final String PROTOCOL_VERSION = "FIX.4.4";
    public static final String SIDE_BUY = "1";
    public static final String SIDE_SELL = "2";
    public static final String MSG_TYPE_NEW_ORDER_SINGLE = "D";
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
        public static final int CHECK_SUM = 10;
        public static final int MSG_SEQ_NUM = 34;
        public static final int MSG_TYPE = 35;
        public static final int ORDER_QTY = 38;
        public static final int PRICE = 44;
        public static final int SENDER_ID = 49;
        public static final int SEND_TIME = 52;
        public static final int SIDE = 54;
        public static final int TICKER = 55;
        public static final int TARGET_ID = 56;
    }

    public static class Header {
        int bodyLength;
        String beginString;

        public Header() {
        }

        public Header(Body body) {
            this.beginString = PROTOCOL_VERSION;
            this.bodyLength = body.toString(false).length() + 1;
        }

        @Override
        public String toString() {
            return String.join(DELIMITER, new String[]{
                    field(FieldKey.BEGIN_STRING, beginString),
                    field(FieldKey.BODY_LENGTH, Integer.toString(bodyLength))
            });
        }
    }

    public static class Body {
        String senderId;
        String msgSeqNum;
        String msgType;
        String side;
        String sendTime;
        String targetId;
        String ticker;
        String orderQty;
        String price;
        String checkSum;

        public String toString(boolean withSum) {
            return String.join(DELIMITER, listOfNotNull(
                    field(FieldKey.MSG_TYPE, msgType),
                    field(FieldKey.MSG_SEQ_NUM, msgSeqNum),
                    field(FieldKey.SENDER_ID, senderId),
                    field(FieldKey.TARGET_ID, targetId),
                    field(FieldKey.SEND_TIME, sendTime),
                    field(FieldKey.SIDE, side),
                    field(FieldKey.TICKER, ticker),
                    field(FieldKey.ORDER_QTY, orderQty),
                    field(FieldKey.PRICE, price),
                    ((withSum) ? field(FieldKey.CHECK_SUM, checkSum) : null)
            ));
        }
    }
}
