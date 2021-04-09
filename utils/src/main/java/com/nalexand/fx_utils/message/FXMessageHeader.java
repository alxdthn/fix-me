package com.nalexand.fx_utils.message;

import static com.nalexand.fx_utils.message.FXMessage.PROTOCOL_VERSION;
import static com.nalexand.fx_utils.message.FXMessageField.BEGIN_STRING;
import static com.nalexand.fx_utils.message.FXMessageField.BODY_LENGTH;

class FXMessageHeader extends FXMessagePart {

    public FXMessageHeader() {
        super();
    }

    public FXMessageHeader(FXMessageBody body) {
        super();
        setBeginString(PROTOCOL_VERSION);
        setBodyLength(Integer.toString(body.toFixString().length() + 1));
    }

    @Override
    protected void createFields() {
        addField(BEGIN_STRING);
        addField(BODY_LENGTH);
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
}
