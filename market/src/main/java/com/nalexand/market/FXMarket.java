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
                                createBuy(fxMessage);
                                break;
                            case FXMessage.SIDE_SELL:
                                createSell(fxMessage);
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

        private void createBuy(FXMessage fxMessage) {
            MarketInstrument marketInstrument = marketData.get(fxMessage.body.getTicker());
            if (marketInstrument == null) {
                logMessage("Can't create BUY %s - no instrument in market\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage);
                sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            marketInstrument.addBuyPosition(
                    fxMessage.header.getSenderId(),
                    new BigInteger(fxMessage.body.getOrderQty()),
                    new BigDecimal(fxMessage.body.getPrice())
            );
            /*
            BigDecimal requestedPrice = new BigDecimal(fxMessage.body.getPrice());
            if (requestedPrice.compareTo(marketInstrument.price) != 0) {
                logMessage("Can't execute BUY %s - bad price %s\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage.body.getPrice(), fxMessage);
                sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            BigInteger requestedQuantity = new BigInteger(fxMessage.body.getOrderQty());
            if (requestedQuantity.compareTo(marketInstrument.quantity) > 0) {
                logMessage("Can't execute BUY %s - to much quantity %s\nmsg: %s",
                        fxMessage.body.getOrderQty(), fxMessage);
                sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            marketInstrument.quantity = marketInstrument.quantity.subtract(requestedQuantity);
            logMessage("Executed BUY");
            printMarketDataInfo(marketInstrument.ticker);
            FXMessage answer = FXMessageFactory.create(
                    FXMessage.SIDE_SELL,
                    fxMessage.header.getSenderId(),
                    fxMessage.body.getTicker(),
                    fxMessage.body.getOrderQty(),
                    fxMessage.body.getPrice()
            );
            answer.body.setOrderStatus(FXMessage.ORDER_STATUS_CALCULATED);
            sendMessage(answer);*/
        }

        private void createSell(FXMessage fxMessage) {
            MarketInstrument marketInstrument = marketData.get(fxMessage.body.getTicker());

            if (marketInstrument == null) {
                logMessage("Can't execute SELL %s - no instrument in market\nmsg: %s",
                        fxMessage.body.getTicker(), fxMessage);
                sendMessage(
                        FXMessageFactory.createRejected(fxMessage)
                );
                return;
            }
            marketInstrument.addSellPosition(
                    fxMessage.header.getSenderId(),
                    new BigInteger(fxMessage.body.getOrderQty()),
                    new BigDecimal(fxMessage.body.getPrice())
            );
//            BigDecimal requestedPrice = new BigDecimal(fxMessage.body.getPrice());
//            if (requestedPrice.compareTo(marketInstrument.price) != 0) {
//                logMessage("Can't execute SELL %s - bad price %s\nmsg: %s",
//                        fxMessage.body.getTicker(), fxMessage.body.getPrice(), fxMessage);
//                sendMessage(
//                        FXMessageFactory.createRejected(fxMessage)
//                );
//                return;
//            }
//            BigInteger requestedQuantity = new BigInteger(fxMessage.body.getOrderQty());
//            marketInstrument.quantity = marketInstrument.quantity.add(requestedQuantity);
//            logMessage("Executed SELL");
//            printMarketDataInfo(marketInstrument.ticker);
//            FXMessage answer = FXMessageFactory.create(
//                    FXMessage.SIDE_SELL,
//                    fxMessage.header.getSenderId(),
//                    fxMessage.body.getTicker(),
//                    fxMessage.body.getOrderQty(),
//                    fxMessage.body.getPrice()
//            );
//            answer.body.setOrderStatus(FXMessage.ORDER_STATUS_CALCULATED);
//            sendMessage(answer);
        }

        private void sendMessage(FXMessage fxMessage) {
            database.saveTransaction(fxMessage);
            client.sendMessage(fxMessage);
        }
    }
}
