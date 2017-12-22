package com.ubb.ppd.lab4.server.domain;

import com.ubb.ppd.lab4.server.model.Invoice;
import com.ubb.ppd.lab4.server.model.Order;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class CommandPlacer implements Runnable {
    private Store        store;
    private List<String> codes;
    private Logger       logger;

    public CommandPlacer(Store store, Logger logger) {
        this.store = store;
        this.codes = new ArrayList<>(store.getAllProductCodes());
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.info("Placing a command from thread " + Thread.currentThread().getName());
        Random  random   = new SecureRandom();
        String  code     = codes.get(random.nextInt(codes.size()));
        Integer quantity = random.nextInt(Store.STOCK_MAX_QUANTITY);

        Optional<Invoice> invoiceOptional = store.processOrderSilenceErrors(
                new Order(code, quantity, System.currentTimeMillis())
        );

        invoiceOptional.ifPresent(invoice -> store.addProfit(invoice.getTotal()));
    }
}
