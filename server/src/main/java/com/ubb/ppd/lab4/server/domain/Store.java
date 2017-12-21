package com.ubb.ppd.lab4.server.domain;

import com.github.javafaker.Faker;
import com.ubb.ppd.lab4.server.model.Invoice;
import com.ubb.ppd.lab4.server.model.Order;
import com.ubb.ppd.lab4.server.model.Product;
import com.ubb.ppd.lab4.server.model.StockItem;
import com.ubb.ppd.lab4.server.repository.*;
import com.ubb.ppd.lab4.server.util.Money;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Marius Adam
 */
public class Store {
    public static final  int    STOCK_MAX_QUANTITY  = 100;
    private static final String DUMP_DATA_SEPARATOR = "================================================";
    private static final String DUMP_DATA_INDENT    = "        ";

    private final ProductRepository   productRepository   = new ProductRepository();
    private final StockItemRepository stockItemRepository = new StockItemRepository();
    private final InvoiceRepository   invoiceRepository   = new InvoiceRepository();
    private final OrderRepository     orderRepository     = new OrderRepository();

    private final OrderProcessor orderProcessor;
    private final PrintWriter    output;
    private final PrintWriter    error;

    private Money lifetimeProfit = new Money("0");


    public Store(OutputStream outputStream, int numberOfProducts) {
        this(outputStream, outputStream, numberOfProducts);
    }

    public Store(OutputStream outputStream, OutputStream errorStream, int numberOfProducts) {
        this.output = new PrintWriter(outputStream);
        this.error = new PrintWriter(errorStream);
        this.orderProcessor = new OrderProcessor(
                productRepository,
                orderRepository,
                invoiceRepository,
                stockItemRepository
        );
        create(numberOfProducts);
    }

    private void create(int numberOfProducts) {
        Faker faker = new Faker(new SecureRandom());

        for (int i = 0; i < numberOfProducts; i++) {
            Product product = new Product(
                    faker.code().asin(),
                    faker.commerce().productName(),
                    new Money(faker.commerce().price(10, 1000)),
                    "piece"
            );
            productRepository.save(product);
            stockItemRepository.save(new StockItem(
                    product.getCode(),
                    faker.number().numberBetween(0, STOCK_MAX_QUANTITY)
            ));
        }
    }

    public Money getLifetimeProfit() {
        return lifetimeProfit;
    }

    /**
     * Dump the state of the store in the given output stream
     *
     * @param outputStream The output stream where to write
     */
    public void dump(OutputStream outputStream) {
        dump(new PrintWriter(outputStream));

    }

    public void dump() {
        dump(output);
    }

    public void dump(PrintWriter writer) {
        writeItems(writer, "Products", productRepository);
        writeItems(writer, "Stock items", stockItemRepository);
        writeItems(writer, "Orders", orderRepository);
        writeItems(writer, "Bills", invoiceRepository);
    }

    private void writeItems(PrintWriter writer, String header, AbstractRepository<?, ?> items) {
        writer.println(header + ":");
        items.findAll().forEach(o -> writer.println(DUMP_DATA_INDENT + o));
        writer.println(DUMP_DATA_SEPARATOR);
        writer.flush();
    }

    public Collection<String> getAllProductCodes() {
        return productRepository
                .findAll()
                .stream()
                .map(Product::getCode)
                .collect(Collectors.toList());
    }

    public void processOrder(Order order) {
        info("Placed order " + order);

        CompletableFuture
                .supplyAsync(() -> orderProcessor.process(order))
                .thenAccept(this::onOrderProcessed)
                .handle((aVoid, throwable) -> {
                    this.onOrderError(order, throwable);
                    return null;
                });
    }

    private void error(String message) {
        Date date = new Date();
        error.println(date + " " + message);
        error.flush();
    }

    private void onOrderProcessed(Invoice invoice) {
        info("Created " + invoice);

        // this is thread safe because Moneys are immutable
        lifetimeProfit = lifetimeProfit.plus(invoice.getTotal());

    }

    private void onOrderError(Order order, Throwable t) {
        error(t.getMessage());
        order.cancel();
        error("Canceled order " + order);
    }

    private void info(String s) {
        Date date = new Date();
        output.println((date + " " + s));
        output.flush();
    }

    public long soldProducts() {
        return invoiceRepository.count();
    }

    public long createdOrders() {
        return orderRepository.count(Order.Status.CREATED);
    }

    public long canceledOrders() {
        return orderRepository.count(Order.Status.CANCELED);
    }

    public long processedOrders() {
        return orderRepository.count(Order.Status.PROCESSED);
    }

    public long totalOrders() {
        return orderRepository.count();
    }

    public long availableProducts() {
        return stockItemRepository.availableProductsCount();
    }

    public long totalProducts() {
        return productRepository.count();
    }
}
