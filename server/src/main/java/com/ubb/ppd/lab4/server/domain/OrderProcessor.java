package com.ubb.ppd.lab4.server.domain;

import com.ubb.ppd.lab4.server.model.Invoice;
import com.ubb.ppd.lab4.server.model.Order;
import com.ubb.ppd.lab4.server.model.Product;
import com.ubb.ppd.lab4.server.model.StockItem;
import com.ubb.ppd.lab4.server.repository.InvoiceRepository;
import com.ubb.ppd.lab4.server.repository.OrderRepository;
import com.ubb.ppd.lab4.server.repository.ProductRepository;
import com.ubb.ppd.lab4.server.repository.StockItemRepository;

/**
 * @author Marius Adam
 */
public class OrderProcessor {
    private final ProductRepository   productRepository;
    private final OrderRepository     orderRepository;
    private final InvoiceRepository   invoiceRepository;
    private final StockItemRepository stockItemRepository;

    public OrderProcessor(ProductRepository productRepository, OrderRepository orderRepository, InvoiceRepository invoiceRepository, StockItemRepository stockItemRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.stockItemRepository = stockItemRepository;
    }

    public Invoice process(Order order) {
        orderRepository.save(order);
        StockItem stockItem = stockItemRepository.findByProductCode(order.getProductCode());
        Product product = productRepository.findByCode(order.getProductCode());
        stockItem.sell(order);
        Invoice invoice = new Invoice(product, order);
        invoiceRepository.save(invoice);
        return invoice;
    }
}
