package com.nalexand.fx_utils.message;

import static com.nalexand.fx_utils.message.FXMessageField.*;

public class FXMessageBody extends FXMessagePart {

    public FXMessageBody() {
        super();
    }

    public FXMessageBody(FXMessageBody from) {
        super();
        setOrderQty(from.getOrderQty());
        setOrderStatus(from.getOrderStatus());
        setPrice(from.getPrice());
        setSide(from.getSide());
        setTicker(from.getTicker());
    }

    @Override
    protected void createFields() {
        addField(SIDE);
        addField(ORDER_STATUS);
        addField(TICKER);
        addField(ORDER_QTY);
        addField(PRICE);
    }

    public String getSide() {
        return getValue(SIDE);
    }

    public void setSide(String side) {
        setValue(SIDE, side);
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