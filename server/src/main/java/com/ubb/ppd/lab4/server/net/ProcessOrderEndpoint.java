package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.domain.exception.DomainException;
import com.ubb.ppd.lab4.server.model.Invoice;
import com.ubb.ppd.lab4.server.model.Order;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class ProcessOrderEndpoint extends RequestResponseEndpoint {
    private final Store           store;
    private final ExecutorService computationExecutor;

    public ProcessOrderEndpoint(int exposedPort, Mode servingMode, Logger logger, Store store) throws IOException {
        super(exposedPort, servingMode, logger);
        this.store = store;
        this.computationExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void close() throws IOException {
        super.close();
        computationExecutor.shutdown();
    }

    @Override
    protected void doServe(Scanner input, PrintWriter writer) {
        Order order = null;
        try {

            String  productCode = input.nextLine();
            Integer quantity    = Integer.parseInt(input.nextLine());

            order = new Order(productCode, quantity, System.currentTimeMillis());
            logger.info("Placed order: " + order);
            writer.println("Received order " + order);

            Invoice invoice = store.processOrder(order);
            onOrderProcessed(invoice);

            writer.println("Order processed, created invoice " + invoice);
        } catch (DomainException e) {
            if (order != null) {
                onOrderError(order, e);
            }

            throw e;
        }
    }

    private void onOrderProcessed(Invoice invoice) {
        logger.info("Created " + invoice);

        CompletableFuture
                .runAsync(() -> {
                    // this is thread safe because Moneys are immutable
                    store.addProfit(invoice.getTotal());
                }, computationExecutor)
                .handle((aVoid, throwable) -> {
                    logger.severe(throwable.getMessage());
                    return null;
                })
                .thenAccept(aVoid -> logger.info("Updated profit " + store.getLifetimeProfit()));
    }

    private void onOrderError(Order order, Throwable t) {
        order.cancel();
        logger.severe(String.format("Canceled order %s : %s", order, t.getMessage()));
    }
}
