package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.model.Invoice;
import com.ubb.ppd.lab4.server.model.Order;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class ProcessOrderEndpoint extends Endpoint {
    private final Store    store;
    private final Executor ordersExecutor;
    private       Logger   logger;
    private       Executor computationExecutor;

    public ProcessOrderEndpoint(int port, Store store, Logger logger) throws IOException {
        this(port, Runtime.getRuntime().availableProcessors(), store, logger);
    }

    ProcessOrderEndpoint(int port, int threadsCount, Store store, Logger logger) throws IOException {
        super(port, logger);
        this.logger = logger;
        this.store = store;
        this.ordersExecutor = Executors.newFixedThreadPool(threadsCount);
        this.computationExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void serve(Socket clientSocket) throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
        try {
            Scanner scanner     = new Scanner(clientSocket.getInputStream());
            String  productCode = scanner.nextLine();
            Integer quantity    = Integer.parseInt(scanner.nextLine());
            Order   order       = new Order(productCode, quantity, System.currentTimeMillis());

            logger.info("Placed order: " + order);

            CompletableFuture
                    .supplyAsync(() -> store.processOrder(order), ordersExecutor)
                    .thenAccept((i) -> {
                        onOrderProcessed(i);
                        writer.println("Order processed, created bill " + i);
                    })
                    .handle((aVoid, throwable) -> {
                        this.onOrderError(order, throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.severe(e.getMessage());
            writer.println(e.getMessage());
        }
    }

    private void onOrderProcessed(Invoice invoice) {
        logger.info("Created " + invoice);

        CompletableFuture
                .supplyAsync((Supplier<Void>) () -> {
                    // this is thread safe because Moneys are immutable
                    store.addProfit(invoice.getTotal());
                    return null;
                }, computationExecutor)
                .handle((aVoid, throwable) -> {
                    logger.severe(throwable.getMessage());
                    return null;
                });
    }

    private void onOrderError(Order order, Throwable t) {
        order.cancel();
        logger.severe("Canceled order " + order);
        logger.severe(t.getMessage());
    }
}