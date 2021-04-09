package com.nalexand.market;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MarketInstrument {

    String ticker;
    BigInteger quantity;
    BigDecimal price;

    public MarketInstrument(String ticker, BigInteger quantity, BigDecimal price) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("ticker: %-12s qty: %-12s price %-12s", ticker, quantity, price);
    }
}
