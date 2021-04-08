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
            int messageNum,
            LocalDateTime currentTime,
            String transactionType,
            String assignedId,
            String market,
            String ticker,
            String quantity,
            String price
    ) {
        FXMessage.Body body = new FXMessage.Body(
                assignedId,
                Integer.toString(messageNum),
                MSG_TYPE_NEW_REQUEST,
                dateTimeFormatter.format(currentTime),
                transactionType,
                market,
                ticker,
                quantity,
                price
        );
        FXMessage.Header header = new FXMessage.Header(body);
        FXMessage result = new FXMessage(header, body);
        calculateSum(result);
        return result;
    }

    public static FXMessage createLogon(
            String assignedId,
            LocalDateTime currentTime
    ) {
        FXMessage.Body body = new FXMessage.Body(
                assignedId,
                null,
                MSG_TYPE_LOGON,
                dateTimeFormatter.format(currentTime),
                null,
                null,
                null,
                null,
                null
        );
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
                case FXMessage.FieldKey.SUM:
                    body.sum = fieldValue;
                    break;
                case FXMessage.FieldKey.MSG_NUM:
                    body.messageNum = fieldValue;
                    break;
                case FXMessage.FieldKey.MSG_TYPE:
                    body.messageType = fieldValue;
                    break;
                case FXMessage.FieldKey.QUANTITY:
                    body.quantity = fieldValue;
                    break;
                case FXMessage.FieldKey.PRICE:
                    body.price = fieldValue;
                    break;
                case FXMessage.FieldKey.SENDER_ID:
                    body.assignedId = fieldValue;
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
                case FXMessage.FieldKey.TRANSACTION_TYPE:
                    body.transactionType = fieldValue;
                    break;
                case FXMessage.FieldKey.TICKER:
                    body.ticker = fieldValue;
                    break;
                case FXMessage.FieldKey.MARKET:
                    body.market = fieldValue;
                    break;
            }
            partCounter++;
        }
        if (!listOfNotNull(
                MSG_TYPE_LOGON,
                MSG_TYPE_NEW_REQUEST
        ).contains(body.messageType)) return new FXMessage(String.format(
                "Bad message type: %s", body.messageType
        ));
        return new FXMessage(header, body);
    }

    private static void calculateSum(FXMessage result) {
        String message = result.header.toString() + DELIMITER + result.body.toString(false) + DELIMITER;
        int sum = 0;

        for (char character : message.toCharArray()) {
            sum += character;
        }
        result.body.sum = Integer.toString(sum % 256);
    }
}
