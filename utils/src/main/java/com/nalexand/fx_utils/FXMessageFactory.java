package com.nalexand.fx_utils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.nalexand.fx_utils.CommonUtils.listOfNotNull;
import static com.nalexand.fx_utils.FXMessage.*;

public class FXMessageFactory {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
    private static final Pattern partValidationRegex = Pattern.compile("\\d+=[A-Za-z.:\\-0-9]+");

    public static FXMessage createRequest(
            int msgSeqNum,
            LocalDateTime currentTime,
            String side,
            String assignedId,
            String market,
            String ticker,
            String orderQty,
            String price
    ) {
        FXMessage.Body body = new FXMessage.Body();
        body.senderId = assignedId;
        body.msgSeqNum = Integer.toString(msgSeqNum);
        body.msgType = MSG_TYPE_NEW_ORDER_SINGLE;
        body.sendTime = dateTimeFormatter.format(currentTime);
        body.side = side;
        body.targetId = market;
        body.ticker = ticker;
        body.orderQty = orderQty;
        body.price = price;
        FXMessage.Header header = new FXMessage.Header(body);
        FXMessage result = new FXMessage(header, body);
        calculateSum(result);
        return result;
    }

    public static FXMessage createLogon(
            String assignedId,
            LocalDateTime currentTime
    ) {
        FXMessage.Body body = new FXMessage.Body();
        body.senderId = assignedId;
        body.msgType = MSG_TYPE_LOGON;
        body.sendTime = dateTimeFormatter.format(currentTime);

        FXMessage.Header header = new FXMessage.Header(body);
        FXMessage result = new FXMessage(header, body);
        calculateSum(result);
        return result;
    }

    public static FXMessage fromString(String message) {
        return fromBytes(message.getBytes());
    }

    public static FXMessage fromBytes(byte[] bytes) {
        String rawMessage = new String(bytes).trim();
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
                case FXMessage.FieldKey.BEGIN_STRING:
                    if (partCounter != 1 || !PROTOCOL_VERSION.equals(fieldValue)) {
                        return new FXMessage(String.format(
                                "Bad version or part position: %d %s (expected %s)",
                                partCounter,
                                fieldValue,
                                PROTOCOL_VERSION
                        ));
                    }
                    header.beginString = fieldValue;
                    break;
                case FXMessage.FieldKey.BODY_LENGTH:
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
                case FXMessage.FieldKey.CHECK_SUM:
                    body.checkSum = fieldValue;
                    break;
                case FXMessage.FieldKey.MSG_SEQ_NUM:
                    body.msgSeqNum = fieldValue;
                    break;
                case FXMessage.FieldKey.MSG_TYPE:
                    body.msgType = fieldValue;
                    break;
                case FXMessage.FieldKey.ORDER_QTY:
                    body.orderQty = fieldValue;
                    break;
                case FXMessage.FieldKey.PRICE:
                    body.price = fieldValue;
                    break;
                case FXMessage.FieldKey.SENDER_ID:
                    body.senderId = fieldValue;
                    break;
                case FXMessage.FieldKey.SEND_TIME:
                    try {
                        dateTimeFormatter.format(dateTimeFormatter.parse(fieldValue));
                    } catch (DateTimeException e) {
                        return new FXMessage(String.format(
                                "Bad time: %s", fieldValue
                        ));
                    }
                    body.sendTime = fieldValue;
                    break;
                case FXMessage.FieldKey.SIDE:
                    body.side = fieldValue;
                    break;
                case FXMessage.FieldKey.TICKER:
                    body.ticker = fieldValue;
                    break;
                case FXMessage.FieldKey.TARGET_ID:
                    body.targetId = fieldValue;
                    break;
            }
            partCounter++;
        }
        if (!listOfNotNull(
                MSG_TYPE_LOGON,
                MSG_TYPE_NEW_ORDER_SINGLE
        ).contains(body.msgType)) return new FXMessage(String.format(
                "Bad message type: %s", body.msgType
        ));
        return new FXMessage(header, body);
    }

    private static void calculateSum(FXMessage result) {
        String message = result.header.toString() + DELIMITER + result.body.toString(false) + DELIMITER;
        int sum = 0;

        for (char character : message.toCharArray()) {
            sum += character;
        }
        result.body.checkSum = Integer.toString(sum % 256);
    }
}
