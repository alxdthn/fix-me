package com.nalexand.fx_utils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.nalexand.fx_utils.Utils.field;
import static com.nalexand.fx_utils.Utils.listOfNotNull;

public class FXMessage {

    public static final String PROTOCOL_VERSION = "FIX.4.4";
    public static final String TRANSACTION_TYPE_BUY = "1";
    public static final String TRANSACTION_TYPE_SELL = "?"; //  TODO
    public static final String DELIMITER = "\u0001";

    private static final Pattern partValidationRegex = Pattern.compile("\\d+=[A-Za-z.:\\-0-9]+");

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    public String error = null;
    public Header header = null;
    public Body body = null;

    private FXMessage(String error) {
        this.error = error;
    }

    private FXMessage(Header header, Body body) {
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

    public static FXMessage createRequest(
            int messageNum,
            LocalDateTime currentTime,
            String transactionType,
            String assignedId,
            String market,
            String ticker,
            String quantity,
            String price
    ) {
        Body body = new Body(
                assignedId,
                Integer.toString(messageNum),
                dateTimeFormatter.format(currentTime),
                transactionType,
                market,
                ticker,
                quantity,
                price
        );
        Header header = new Header(body);
        FXMessage result = new FXMessage(header, body);
        result.calculateSum();
        return result;
    }

    public static FXMessage fromBytes(byte[] bytes) {
        String rawMessage = new String(bytes);
        String[] split = rawMessage.split("\\x01");

        FXMessage.Header header = new FXMessage.Header();
        FXMessage.Body body = new FXMessage.Body();

        int partCounter = 1;
        for (String str : split) {
            if (!partValidationRegex.matcher(str).matches()) {
                return new FXMessage(String.format(
                        "Bad part: %s", str
                ));
            }
            String[] field = str.split("=");
            int fieldKey = Integer.parseInt(field[0]);
            String fieldValue = field[1];

            switch (fieldKey) {
                case FieldKey.BEGIN_STRING:
                    if (partCounter != 1 && !PROTOCOL_VERSION.equals(fieldValue)) {
                        return new FXMessage(String.format(
                                "Bad version or part position: %d %s (expected %s)",
                                partCounter,
                                fieldKey,
                                PROTOCOL_VERSION
                        ));

                    }
                    header.beginString = fieldValue;
                    break;
                case FieldKey.BODY_LENGTH:
                    if (partCounter != 2) {
                        return new FXMessage(String.format(
                                "Bad length position %d",
                                partCounter
                        ));
                    }
                    try {
                        header.bodyLength = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException e) {
                        return new FXMessage(String.format(
                                "Bad body length %s", fieldValue
                        ));
                    }
                    break;
                case FieldKey.SUM:
                    body.sum = fieldValue;
                    break;
                case FieldKey.MSG_NUM:
                    body.messageNum = fieldValue;
                    break;
                case FieldKey.QUANTITY:
                    body.quantity = fieldValue;
                    break;
                case FieldKey.PRICE:
                    body.price = fieldValue;
                    break;
                case FieldKey.SENDER_ID:
                    body.assignedId = fieldValue;
                    break;
                case FieldKey.SEND_TIME:
                    try {
                        dateTimeFormatter.format(dateTimeFormatter.parse(fieldValue));
                    } catch (DateTimeException e) {
                        return new FXMessage(String.format(
                                "Bad time: %s", fieldValue
                        ));
                    }
                    body.sendTime = fieldValue;
                    break;
                case FieldKey.TRANSACTION_TYPE:
                    body.transactionType = fieldValue;
                    break;
                case FieldKey.TICKER:
                    body.ticker = fieldValue;
                    break;
                case FieldKey.MARKET:
                    body.market = fieldValue;
                    break;
            }
            partCounter++;
        }
        return new FXMessage(header, body);
    }

    private void calculateSum() {
        String message = header.toString() + DELIMITER + body.toString(false) + DELIMITER;
        int result = 0;

        for (char character : message.toCharArray()) {
            result += character;
        }
        body.sum = Integer.toString(result % 256);
    }

    public static class FieldKey {

        private FieldKey() {
        }

        public static final int BEGIN_STRING = 8;
        public static final int BODY_LENGTH = 9;
        public static final int SUM = 10;
        public static final int MSG_NUM = 34;
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
        String transactionType;
        String sendTime;
        String market;
        String ticker;
        String quantity;
        String price;
        String sum;

        public Body() {
        }

        public Body(String assignedId, String messageNum, String sendTime, String transactionType, String market, String ticker, String quantity, String price) {
            this.assignedId = assignedId;
            this.messageNum = messageNum;
            this.transactionType = transactionType;
            this.sendTime = sendTime;
            this.market = market;
            this.ticker = ticker;
            this.quantity = quantity;
            this.price = price;
        }

        public String toString(boolean withSum) {
            return String.join(DELIMITER, listOfNotNull(
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
