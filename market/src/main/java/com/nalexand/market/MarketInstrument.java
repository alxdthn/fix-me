package com.nalexand.market;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.stream.Collectors;

import static com.nalexand.fx_utils.CommonUtils.listOfNotNull;

public class MarketInstrument {

    String ticker;

    private Queue<InstrumentPosition> buyPositions = new ArrayDeque<>();

    private Queue<InstrumentPosition> sellPositions = new ArrayDeque<>();

    public MarketInstrument(String ticker) {
        this.ticker = ticker;
    }

    public void addBuyPosition(String ownerId, BigInteger quantity, BigDecimal price) {
        
        buyPositions.add(
                new InstrumentPosition(
                        ownerId,
                        quantity,
                        price
                )
        );
    }

    public void addSellPosition(String ownerId, BigInteger quantity, BigDecimal price) {
        sellPositions.add(
                new InstrumentPosition(
                        ownerId,
                        quantity,
                        price
                )
        );
    }

    public String format() {
        if (buyPositions.isEmpty() && sellPositions.isEmpty()) return null;
        return String.join("\n", listOfNotNull(
                String.format("ticker: %s", ticker),
                positionsToString("buy positions", buyPositions),
                positionsToString("sell positions", sellPositions)
        ));
    }

    private String positionsToString(String title, Collection<InstrumentPosition> positions) {
        if (positions.isEmpty()) return null;
        return String.format("%s:\n", title) + positions
                .stream()
                .map(InstrumentPosition::toString)
                .collect(Collectors.joining("\n"));
    }

    private static class InstrumentPosition {

        private String ownerId;
        private BigInteger quantity;
        private BigDecimal price;

        public InstrumentPosition(String ownerId, BigInteger quantity, BigDecimal price) {
            this.ownerId = ownerId;
            this.quantity = quantity;
            this.price = price;
        }

        @Override
        public String toString() {
            return String.format("owner = %s; qty = %s; price = %.2f$",
                    ownerId,
                    quantity.toString(),
                    price.doubleValue()
            );
        }
    }
}
