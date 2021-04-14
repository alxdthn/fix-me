package com.nalexand.market;

import com.nalexand.fx_utils.FXClient;
import com.nalexand.fx_utils.message.FXMessage;
import com.nalexand.fx_utils.message.FXMessageFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

public class FXMarket {

    private static final Map<String, MarketInstrument> marketData = new Hashtable<>();

    public static void main(String[] args) {
        createMarketData();

        Market market = new Market();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String line = scanner.nextLine();
                String[] lineSplit = line.split("\\s+");
                if (lineSplit.length > 0) {
                    logStatus((lineSplit.length > 1) ? lineSplit[1] : null);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void createMarketData() {
        addMarketInstrument("TSLA", "1000", "100.00");
    }

    private static void addMarketInstrument(String ticker, String quantity, String price) {
        MarketInstrument marketInstrument = new MarketInstrument(
                ticker,
                new BigInteger(quantity),
                new BigDecimal(price)
        );
        marketData.put(marketInstrument.ticker, marketInstrument);
    }

    public static void logStatus(String ticker) {
        if (ticker == null) {
            marketData.values().forEach(marketInstrument -> logStatus(marketInstrument.ticker));
            return;
        }
        MarketInstrument marketInstrument = marketData.get(ticker);
        if (marketInstrument == null) {
            logMessage("No no instrument in market %s", ticker);
            return;
        }
        logMessage("Status: \n%s", marketInstrument);
    }

    public static void logMessage(String message, Object... args) {
        Object[] nexArgs = Arrays.stream(args).toArray();
        System.out.printf("MARKET: %s\n", String.format(message, nexArgs));
    }

    private static class Market implements FXClient.Listener {

        FXClient client = new FXClient(5000, "MARKET", this);

        @Override
        public void onMessageReceived(FXMessage fxMessage) {
            try {
                if (fxMessage.body.getMsgType().equals(FXMessage.MSG_TYPE_NEW_ORDER_SINGLE)) {
                    switch (fxMessage.body.getSide()) {
                        case FXMessage.SIDE_BUY:
                            executeBuy(fxMessage);
                            break;
                        case FXMessage.SIDE_SELL:
                            executeSell(fxMessage);
                            break;
                    }
                }
            } catch (NullPointerException e) {
                //  TODO bad message received
            }
        }

        @Override
        public void onMessageSendError(FXMessage fxMessage, Throwable e) {

        }

        private void executeBuy(FXMessage fxMessage) {
            MarketInstrument marketInstrument = marketData.get(fxMessage.body.getTicker());
            if (marketInstrument == null) {
                logMessage("Can't execute BUY %s - no instrument in market\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage);
                client.sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            BigDecimal requestedPrice = new BigDecimal(fxMessage.body.getPrice());
            if (requestedPrice.compareTo(marketInstrument.price) != 0) {
                logMessage("Can't execute BUY %s - bad price %s\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage.body.getPrice(), fxMessage);
                client.sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            BigInteger requestedQuantity = new BigInteger(fxMessage.body.getOrderQty());
            if (requestedQuantity.compareTo(marketInstrument.quantity) > 0) {
                logMessage("Can't execute BUY %s - to much quantity %s\nmsg: %s",
                        fxMessage.body.getOrderQty(), fxMessage);
                client.sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            marketInstrument.quantity = marketInstrument.quantity.subtract(requestedQuantity);
            logMessage("Executed BUY");
            logStatus(marketInstrument.ticker);
            FXMessage answer = FXMessageFactory.create(
                    FXMessage.SIDE_SELL,
                    fxMessage.body.getSenderId(),
                    fxMessage.body.getTicker(),
                    fxMessage.body.getOrderQty(),
                    fxMessage.body.getPrice()
            );
            answer.body.setOrderStatus(FXMessage.ORDER_STATUS_CALCULATED);
            client.sendMessage(answer);
        }

        private void executeSell(FXMessage fxMessage) {
            MarketInstrument marketInstrument = marketData.get(fxMessage.body.getTicker());
            if (marketInstrument == null) {
                client.sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                logMessage("Can't execute SELL %s - no instrument in market\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage);
                return;
            }
            BigDecimal requestedPrice = new BigDecimal(fxMessage.body.getPrice());
            if (requestedPrice.compareTo(marketInstrument.price) != 0) {
                client.sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                logMessage("Can't execute SELL %s - bad price %s\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage.body.getPrice(), fxMessage);
                return;
            }
            BigInteger requestedQuantity = new BigInteger(fxMessage.body.getOrderQty());
            marketInstrument.quantity = marketInstrument.quantity.add(requestedQuantity);
            logMessage("Executed SELL");
            logStatus(marketInstrument.ticker);
            FXMessage answer = FXMessageFactory.create(
                    FXMessage.SIDE_SELL,
                    fxMessage.body.getSenderId(),
                    fxMessage.body.getTicker(),
                    fxMessage.body.getOrderQty(),
                    fxMessage.body.getPrice()
            );
            answer.body.setOrderStatus(FXMessage.ORDER_STATUS_CALCULATED);
            client.sendMessage(answer);
        }
    }
}
