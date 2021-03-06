package com.nalexand.fx_utils;

import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

public class FxMessageTest {

    private final LocalDateTime time = LocalDateTime.of(1993, Month.OCTOBER, 16, 23, 42, 12, 111111111);

    @Test
    public void testCreateRequest() {
        String expected = "8=FIX.4.4|9=77|35=D|49=123456|56=CME|52=19931016-23:42:12.111|54=1|55=TSLA|38=42|44=1000.00|10=217|"
                .replace("|", "\u0001");

        String senderId = "123456";
        String side = FXMessage.SIDE_BUY;
        String ticker = "TSLA";
        String targetId = "CME";
        String orderQty = "42";
        String price = "1000.00";

        FXMessage fxMessage = FXMessageFactory.create(
                side,
                targetId,
                ticker,
                orderQty,
                price
        );
        fxMessage.setSendTime(time);
        fxMessage.header.setSenderId(senderId);
        fxMessage.calculateMessageLength();
        fxMessage.calculateSum();

        FXMessage fxMessageFromBytes = FXMessageFactory.fromBytes(expected.getBytes());

        Assert.assertEquals(expected, fxMessage.toFixString());
        Assert.assertEquals(expected, fxMessageFromBytes.toFixString());
    }

    @Test
    public void testCreateLogon() {
        String expected = "8=FIX.4.4|9=40|35=A|49=123456|52=19931016-23:42:12.111|10=150|"
                .replace("|", "\u0001");

        String senderId = "123456";

        FXMessage fxMessage = FXMessageFactory.createLogon();
        fxMessage.setSendTime(time);
        fxMessage.header.setSenderId(senderId);
        fxMessage.calculateMessageLength();
        fxMessage.calculateSum();

        Assert.assertEquals(expected, fxMessage.toFixString());

        FXMessage fxMessageFromBytes = FXMessageFactory.fromBytes(expected.getBytes());
        Assert.assertEquals(expected, fxMessageFromBytes.toFixString());
    }
}
