package com.ubb.ppd.lab4.server;

import com.ubb.ppd.lab4.server.domain.StockChecker;
import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.model.Order;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Marius Adam
 */
public class BootstrapServer {
    private static Store createStore(int productsCount, boolean logEnabled) {
        return new Store(
                logEnabled ? System.out : new NullOutputStream(),
                logEnabled ? System.err : new NullOutputStream(),
                productsCount
        );
    }

    public static void main(String[] args) throws InterruptedException {

        Store store = createStore(10, false);
        StockChecker checker = new StockChecker(store, System.out);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                checker,
                0,
                5,
                TimeUnit.SECONDS
        );

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> store.dump(System.out),
                0,
                5,
                TimeUnit.SECONDS
        );

        List<String> codes = new ArrayList<>(store.getAllProductCodes());


        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    Random random = new SecureRandom();
                    String code = codes.get(random.nextInt(codes.size()));
                    Integer quantity = random.nextInt(Store.STOCK_MAX_QUANTITY);
                    store.processOrder(new Order(code, quantity, System.currentTimeMillis()));
                },
                0,
                100,
                TimeUnit.MILLISECONDS
        );

    }

    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {

        }
    }
}
