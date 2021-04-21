package com.nalexand.fx_utils.message;

import static com.nalexand.fx_utils.message.FXMessage.PROTOCOL_VERSION;
import static com.nalexand.fx_utils.message.FXMessageField.*;

public class FXMessageHeader extends FXMessagePart {

    public FXMessageHeader() {
        super();
        setBeginString(PROTOCOL_VERSION);
    }

    public FXMessageHeader(FXMessageHeader from) {
        super();
        setBeginString(from.getBeginString());
        setBodyLength(from.getBodyLength());
        setTargetId(from.getTargetId());
        setSenderId(from.getSenderId());
        setMsgType(from.getMsgType());
        setSendTime(from.getSendTime());
    }

    @Override
    protected void createFields() {
        addLengthIgnoredField(BEGIN_STRING);
        addLengthIgnoredField(BODY_LENGTH);
        addField(MSG_TYPE);
        addField(SENDER_ID);
        addField(TARGET_ID);
        addField(SEND_TIME);
    }

    public String getBodyLength() {
        return getValue(BODY_LENGTH);
    }

    public void setBodyLength(String bodyLength) {
        setValue(BODY_LENGTH, bodyLength);
    }

    public String getBeginString() {
        return getValue(BEGIN_STRING);
    }

    public void setBeginString(String beginString) {
        setValue(BEGIN_STRING, beginString);
    }

    public String getMsgType() {
        return getValue(MSG_TYPE);
    }

    public void setMsgType(String msgType) {
        setValue(MSG_TYPE, msgType);
    }

    public String getSenderId() {
        return getValue(SENDER_ID);
    }

    public void setSenderId(String senderId) {
        setValue(SENDER_ID, senderId);
    }

    public String getTargetId() {
        return getValue(TARGET_ID);
    }

    public void setTargetId(String targetId) {
        setValue(TARGET_ID, targetId);
    }

    public String getSendTime() {
        return getValue(SEND_TIME);
    }

    public void setSendTime(String sendTime) {
        setValue(SEND_TIME, sendTime);
    }
}
