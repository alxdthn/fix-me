package com.nalexand.fx_utils.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.nalexand.fx_utils.message.FXMessageField.CHECK_SUM;

public class FXMessage extends FXMessagePart {

    public static final String FIX_DELIMITER = "\u0001";
    public static final String USER_DELIMITER = "\n";
    public static final String PROTOCOL_VERSION = "FIX.4.4";
    public static final String SIDE_BUY = "1";
    public static final String SIDE_SELL = "2";
    public static final String MSG_TYPE_NEW_ORDER_SINGLE = "D";
    public static final String MSG_TYPE_LOGON = "A";
    public static final String MSG_TYPE_REJECT = "3";
    public static final String ORDER_STATUS_CALCULATED = "B";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    public String error = null;
    public FXMessageHeader header = null;
    public FXMessageBody body = null;

    FXMessage() {
        super();
        this.header = new FXMessageHeader();
        this.body = new FXMessageBody();
    }

    FXMessage(String error) {
        super();
        this.error = error;
    }

    FXMessage(FXMessage from) {
        super();
        error = from.error;
        header = new FXMessageHeader(from.header);
        body = new FXMessageBody(from.body);
        setCheckSum(from.getCheckSum());
    }

    @Override
    protected void createFields() {
        addField(CHECK_SUM);
    }

    public String getCheckSum() {
        return getValue(CHECK_SUM);
    }

    public void setCheckSum(String checkSum) {
        setValue(CHECK_SUM, checkSum);
    }

    @Override
    public String toFixString() {
        return String.join(FIX_DELIMITER,
                header.toFixString(),
                body.toFixString(),
                super.toFixString()
        );
    }

    @Override
    public String toString() {
        return String.join(USER_DELIMITER,
                header.toUserString(),
                body.toUserString(),
                super.toUserString()
        );
    }

    public byte[] getBytes() {
        return toFixString().getBytes();
    }

    public void calculateSum() {
        String fixString = header.toFixString() + FIX_DELIMITER + body.toFixString();
        int sum = 0;

        for (char character : fixString.toCharArray()) {
            sum += character;
        }
        setCheckSum(Integer.toString(sum % 256));
    }

    public void calculateBodyLength() {
        header.setBodyLength(Integer.toString(body.toFixString().length() + 1));
    }

    public void setSendTime(LocalDateTime time) {
        body.setSendTime(dateTimeFormatter.format(time));
    }

    public void prepare(String senderId) {
        setSendTime(LocalDateTime.now());
        body.setSenderId(senderId);
        calculateSum();
    }
}
