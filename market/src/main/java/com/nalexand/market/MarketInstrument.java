package com.nalexand.market;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.nalexand.fx_utils.CommonUtils.listOfNotNull;

public class MarketInstrument {

    String ticker;

    private final List<InstrumentPosition> buyPositions = new ArrayList<>();

    private final List<InstrumentPosition> sellPositions = new ArrayList<>();

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

    public boolean executeBuy(BigInteger quantity, BigDecimal price) {
        return execute(sellPositions, quantity, price);
    }

    public boolean executeSell(BigInteger quantity, BigDecimal price) {
        return execute(buyPositions, quantity, price);
    }

    public boolean execute(List<InstrumentPosition> positions, BigInteger quantity, BigDecimal price) {
        for (InstrumentPosition position : positions) {
            if (position.price.compareTo(price) == 0 && position.quantity.compareTo(quantity) >= 0) {
                position.quantity = position.quantity.subtract(quantity);
                if (position.quantity.compareTo(BigInteger.ZERO) == 0) {
                    sellPositions.remove(position);
                }
                return true;
            }
        }
        return false;
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
