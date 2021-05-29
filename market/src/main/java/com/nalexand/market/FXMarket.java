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
import java.util.function.BiFunction;

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
                    printMarketDataInfo((lineSplit.length > 1) ? lineSplit[1] : null);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void createMarketData() {
        addMarketInstrument("TSLA");
        addMarketInstrument("GME");
        addMarketInstrument("YNDX");
        addMarketInstrument("MSFT");
        addMarketInstrument("ZM");
        addMarketInstrument("F");
        addMarketInstrument("BA");
    }

    private static void addMarketInstrument(String ticker) {
        MarketInstrument marketInstrument = new MarketInstrument(ticker);
        marketData.put(marketInstrument.ticker, marketInstrument);
    }

    public static void printMarketDataInfo(String ticker) {
        if (ticker == null) {
            marketData.values()
                    .forEach(marketInstrument -> printMarketDataInfo(marketInstrument.ticker));
            return;
        }
        MarketInstrument marketInstrument = marketData.get(ticker);
        if (marketInstrument == null) {
            logMessage("No no instrument in market %s", ticker);
            return;
        }
        String format = marketInstrument.format();
        if (format != null) {
            logMessage("Status: \n%s", marketInstrument.format());
        }
    }

    public static void logMessage(String message, Object... args) {
        Object[] nexArgs = Arrays.stream(args).toArray();
        System.out.printf("MARKET: %s\n", String.format(message, nexArgs));
    }

    private static class Market implements FXClient.Listener {

        private final FXClient client = new FXClient(5000, "MARKET", this);

        private final DataBaseInteractor database = new DataBaseInteractor();

        @Override
        public void onMessageReceived(FXMessage fxMessage) {
            try {
                switch (fxMessage.header.getMsgType()) {
                    case FXMessage.MSG_TYPE_NEW_ORDER_SINGLE:
                        switch (fxMessage.body.getSide()) {
                            case FXMessage.SIDE_BUY:
                                makeBuy(fxMessage);
                                break;
                            case FXMessage.SIDE_SELL:
                                makeSell(fxMessage);
                                break;
                        }
                        break;
                    case FXMessage.MSG_TYPE_LOGON:
                        database.assignedId = fxMessage.header.getTargetId();
                        logMessage("Database: assigned id %s", database.assignedId);
                        break;
                }
            } catch (NullPointerException e) {
                //  TODO bad message received
            }
        }

        @Override
        public void onMessageSendError(FXMessage fxMessage, Throwable e) {

        }

        private void makeBuy(FXMessage fxMessage) {
            MarketInstrument marketInstrument = marketData.get(fxMessage.body.getTicker());
            if (marketInstrument == null) {
                logMessage("Can't create BUY %s - no instrument in market\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage);
                sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            makeTransaction(
                    fxMessage,
                    marketInstrument::executeSell,
                    marketInstrument::addBuyPosition
            );
        }

        private void makeSell(FXMessage fxMessage) {
            MarketInstrument marketInstrument = marketData.get(fxMessage.body.getTicker());

            if (marketInstrument == null) {
                logMessage("Can't execute SELL %s - no instrument in market\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage);
                sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            makeTransaction(
                    fxMessage,
                    marketInstrument::executeBuy,
                    marketInstrument::addSellPosition
            );
        }

        private void makeTransaction(
                FXMessage fxMessage,
                BiFunction<BigInteger, BigDecimal, Boolean> executor,
                Updater<String, BigInteger, BigDecimal> updater
        ) {
            BigInteger quantity = new BigInteger(fxMessage.body.getOrderQty());
            BigDecimal price = new BigDecimal(fxMessage.body.getPrice());
            FXMessage response = new FXMessage(fxMessage);
            response.header.setTargetId(fxMessage.header.getSenderId());
            if (!executor.apply(quantity, price)) {
                updater.update(
                        fxMessage.header.getSenderId(),
                        quantity,
                        price
                );
                response.body.setOrderStatus(FXMessage.ORDER_STATUS_NEW);
            } else {
                response.body.setOrderStatus(FXMessage.ORDER_STATUS_CALCULATED);
            }
            sendMessage(response);
        }

        private void sendMessage(FXMessage fxMessage) {
            database.saveTransaction(fxMessage);
            client.sendMessage(fxMessage);
        }

        private interface Updater<T1, T2, T3> {

            void update(T1 id, T2 price, T3 quantity);
        }
    }
}
