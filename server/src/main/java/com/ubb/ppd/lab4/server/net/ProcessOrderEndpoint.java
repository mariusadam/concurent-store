package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.model.Invoice;
import com.ubb.ppd.lab4.server.model.Order;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class ProcessOrderEndpoint extends AbstractEndpoint {
    public static final int DEFAULT_ACCEPTING_THREADS_COUNT = 10;
    private final Store           store;
    private final Logger          logger;
    private final ExecutorService computationExecutor;
    private final ExecutorService serverExecutor;

    public ProcessOrderEndpoint(int port, Store store, Logger logger) throws IOException {
        this(port, DEFAULT_ACCEPTING_THREADS_COUNT, store, logger);
    }

    ProcessOrderEndpoint(int port, int threadsCount, Store store, Logger logger) throws IOException {
        super(port, logger);
        this.logger = logger;
        this.store = store;
        computationExecutor = Executors.newSingleThreadExecutor();
        serverExecutor = Executors.newFixedThreadPool(threadsCount);
    }

    @Override
    protected void serve(Socket clientSocket) throws IOException {
        CompletableFuture
                .supplyAsync(() -> {
                    try (PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
                         Scanner scanner = new Scanner(clientSocket.getInputStream())) {
                        doServe(scanner, printStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }, serverExecutor)
                .thenAccept(aVoid -> logger.info("Actually finished serving client " + clientSocket.getInetAddress()));
    }

    @Override
    public void close() throws IOException {
        super.close();
        computationExecutor.shutdown();
        serverExecutor.shutdown();
    }

    private void doServe(Scanner scanner, PrintStream printStream) {
        Order order = null;
        try {

            String productCode = scanner.nextLine();
            Integer quantity = Integer.parseInt(scanner.nextLine());

            order = new Order(productCode, quantity, System.currentTimeMillis());
            logger.info("Placed order: " + order);
            printStream.println("Received order " + order);

            Invoice invoice = store.processOrder(order);
            onOrderProcessed(invoice);

            printStream.println("Order processed, created invoice " + invoice);
        } catch (Exception e) {
            if (order != null) {
                onOrderError(order, e);
            }

            printStream.println("Error: " + e.getMessage());
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
                })
                .thenAccept(aVoid -> logger.info("Updated profit " + store.getLifetimeProfit()));
    }

    private void onOrderError(Order order, Throwable t) {
        order.cancel();
        logger.severe("Canceled order " + order);
        logger.severe(String.format("[%s] %s", t.getClass().getName(), t.getMessage()));
    }
}
