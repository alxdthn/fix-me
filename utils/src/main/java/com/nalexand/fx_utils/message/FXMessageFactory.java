package com.nalexand.fx_utils.message;

import com.nalexand.fx_utils.FXClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.nalexand.fx_utils.message.FXMessage.*;

public class FXMessageFactory {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
    private static final Pattern partValidationRegex = Pattern.compile("\\d+=[A-Za-z.:\\-0-9]+");

    public static FXMessage create(
            LocalDateTime currentTime,
            String senderId,
            String msgSeqNum,
            String side,
            String targetId,
            String ticker,
            String orderQty,
            String price
    ) {
        FXMessageBody body = new FXMessageBody();
        body.setMsgType(MSG_TYPE_NEW_ORDER_SINGLE);
        body.setSendTime(dateTimeFormatter.format(currentTime));
        body.setMsgSeqNum(msgSeqNum);
        body.setSide(side);
        body.setSenderId(senderId);
        body.setTargetId(targetId);
        body.setTicker(ticker);
        body.setOrderQty(orderQty);
        body.setPrice(price);
        FXMessageHeader header = new FXMessageHeader(body);
        return new FXMessage(header, body);
    }

    public static FXMessage createLogon(
            String senderId,
            LocalDateTime currentTime
    ) {
        FXMessageBody body = new FXMessageBody();
        body.setSenderId(senderId);
        body.setMsgType(MSG_TYPE_LOGON);
        body.setSendTime(dateTimeFormatter.format(currentTime));

        FXMessageHeader header = new FXMessageHeader(body);
        return new FXMessage(header, body);
    }

    public static FXMessage fromString(String message) {
        return fromBytes(message.getBytes());
    }

    public static FXMessage fromBytes(byte[] bytes) {
        String rawMessage = new String(bytes).trim();
        String[] split = rawMessage.split("\\x01");

        FXMessage message = new FXMessage();

        for (String str : split) {
            if (!partValidationRegex.matcher(str).matches()) {
                return new FXMessage(String.format(
                        "Bad part: %s", str
                ));
            }
            String[] strSplit = str.split("=");
            String value = strSplit[1];
            FXMessageField.Key key = FXMessageField.from(strSplit[0]);

            message.setValue(key, value);
            message.body.setValue(key, value);
            message.header.setValue(key, value);
        }
        return message;
    }

    public static FXMessage fromInput(int msgSeqNum, String input, String senderId) throws FXClient.FXBadMessageException {
        String[] inputSplit = input.split("\\s+");
        String usage = "USAGE: [market] [buy/sell] [ticker] [quantity] [price]";
        try {
            String side;
            switch (inputSplit[1]) {
                case "buy":
                    side = SIDE_BUY;
                    break;
                case "sell":
                    side = SIDE_SELL;
                    break;
                default:
                    throw new FXClient.FXBadMessageException(String.format("Bad input: \"%s\"\n%s", input, usage));
            }

            return create(
                    LocalDateTime.now(),
                    senderId,
                    Integer.toString(msgSeqNum),
                    side,
                    inputSplit[0],
                    inputSplit[2],
                    inputSplit[3],
                    inputSplit[4]
            );
        } catch (IndexOutOfBoundsException e) {
            throw new FXClient.FXBadMessageException(String.format("Bad input: %s\n%s", input, usage));
        }
    }
}
