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
import java.util.Optional;
import java.util.logging.Logger;
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
    private Logger logger;

    private Money lifetimeProfit = new Money("0");

    public Store(int numberOfProducts, Logger logger) {
        this.logger = logger;
        create(numberOfProducts);
    }

    private void create(int numberOfProducts) {
        logger.info("Creating " + numberOfProducts + " products with random data.");
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
        logger.info("Done creating the products and stock items.");
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

    public void dump(PrintWriter writer) {
        writer.println("Date: " + new Date());
        writeItems(writer, "Products", productRepository);
        writeItems(writer, "Stock items", stockItemRepository);
        writeItems(writer, "Orders", orderRepository);
        writeItems(writer, "Bills", invoiceRepository);
        writer.println(DUMP_DATA_SEPARATOR);
        writer.flush();
    }

    private void writeItems(PrintWriter writer, String header, AbstractRepository<?, ?> items) {
        writer.println(header + ":");
        items.findAll().forEach(o -> writer.println(DUMP_DATA_INDENT + o));
    }

    public Collection<String> getAllProductCodes() {
        return productRepository
                .findAll()
                .stream()
                .map(Product::getCode)
                .collect(Collectors.toList());
    }

    public Invoice processOrder(Order order) {
        orderRepository.save(order);
        StockItem stockItem = stockItemRepository.findByProductCode(order.getProductCode());
        Product   product   = productRepository.findByCode(order.getProductCode());

        logger.info("Starting the heavy work for order " + order);
        logger.info("Finished the heavy work for order " + order);

        stockItem.sell(order);

        logger.info("Sold product, creating invoice.");
        Invoice invoice = new Invoice(product, order);
        invoiceRepository.save(invoice);
        logger.info("Invoice " + invoice + " created.");
        return invoice;
    }

    public Optional<Invoice> processOrderSilenceErrors(Order order) {
        try {
            return Optional.of(processOrder(order));
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return Optional.empty();
        }
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

    public void addProfit(Money profit) {
        lifetimeProfit = lifetimeProfit.plus(profit);
    }
}
