package com.nalexand.fx_utils.message;

import static com.nalexand.fx_utils.message.FXMessageField.*;

public class FXMessageBody extends FXMessagePart {

    FXMessageBody() {
        super();
    }

    @Override
    protected void createFields() {
        addField(MSG_TYPE);
        addField(MSG_SEQ_NUM);
        addField(SENDER_ID);
        addField(TARGET_ID);
        addField(SEND_TIME);
        addField(SIDE);
        addField(ORDER_STATUS);
        addField(TICKER);
        addField(ORDER_QTY);
        addField(PRICE);
    }

    public String getSenderId() {
        return getValue(SENDER_ID);
    }

    public void setSenderId(String senderId) {
        setValue(SENDER_ID, senderId);
    }

    public String getMsgSeqNum() {
        return getValue(MSG_SEQ_NUM);
    }

    public void setMsgSeqNum(String msgSeqNum) {
        setValue(MSG_SEQ_NUM, msgSeqNum);
    }

    public String getMsgType() {
        return getValue(MSG_TYPE);
    }

    public void setMsgType(String msgType) {
        setValue(MSG_TYPE, msgType);
    }

    public String getSide() {
        return getValue(SIDE);
    }

    public void setSide(String side) {
        setValue(SIDE, side);
    }

    public String getSendTime() {
        return getValue(SEND_TIME);
    }

    public void setSendTime(String sendTime) {
        setValue(SEND_TIME, sendTime);
    }

    public String getTargetId() {
        return getValue(TARGET_ID);
    }

    public void setTargetId(String targetId) {
        setValue(TARGET_ID, targetId);
    }

    public String getTicker() {
        return getValue(TICKER);
    }

    public void setTicker(String ticker) {
        setValue(TICKER, ticker);
    }

    public String getOrderQty() {
        return getValue(ORDER_QTY);
    }

    public void setOrderQty(String orderQty) {
        setValue(ORDER_QTY, orderQty);
    }

    public String getOrderStatus() {
        return getValue(ORDER_STATUS);
    }

    public void setOrderStatus(String orderStatus) {
        setValue(ORDER_STATUS, orderStatus);
    }

    public String getPrice() {
        return getValue(PRICE);
    }

    public void setPrice(String price) {
        setValue(PRICE, price);
    }
}