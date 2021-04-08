package com.nalexand.fx_utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

public class FxMessageTest {

    @Test
    public void testCreateRequest() {

        int messageNum = 1;
        LocalDateTime time = LocalDateTime.of(1993, Month.OCTOBER, 16, 23, 42, 12, 111111111);
        String assignedId = "123456";
        String transactionType = FXMessage.TRANSACTION_TYPE_BUY;
        String ticker = "TSLA";
        String market = "CME";
        String quantity = "42";
        String price = "1000.00";

        FXMessage fxMessage = FXMessage.createRequest(
                messageNum,
                time,
                transactionType,
                assignedId,
                market,
                ticker,
                quantity,
                price
        );

        String expected = "8=FIX.4.4|9=77|34=1|49=123456|56=CME|52=19931016-23:42:12.111|54=1|55=TSLA|38=42|44=1000.00|10=197"
                .replace("|", "\u0001");

        Assert.assertEquals(expected, fxMessage.toString());
    }

    @Test
    public void testCreateMessageFromBytes() {
        String messageString = "8=FIX.4.4|9=77|34=1|49=123456|56=CME|52=19931016-23:42:12.111|54=1|55=TSLA|38=42|44=1000.00|10=197"
                .replace("|", "\u0001");
        byte[] messageBytes = messageString.getBytes();
        FXMessage fxMessage = FXMessage.fromBytes(messageBytes);

        Assert.assertEquals(messageString, fxMessage.toString());
    }
}
