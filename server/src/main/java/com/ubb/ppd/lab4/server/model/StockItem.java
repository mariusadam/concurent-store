package com.ubb.ppd.lab4.server.model;

import com.ubb.ppd.lab4.server.domain.exception.DomainException;
import com.ubb.ppd.lab4.server.domain.exception.StockUnavailableException;

/**
 * @author Marius Adam
 */
public class StockItem implements Identifiable<String> {
    private final Object mutex = new Object();
    private String productCode;
    private long   quantity;

    public StockItem(String productCode, long quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public String getProductCode() {
        return productCode;
    }

    public long getQuantity() {
        return quantity;
    }

    public void sell(Order order) {
        // protect the critical section from concurrent access
        // using a local mutex to allow other thread to perform reads
        synchronized (mutex) {
            if (!order.getStatus().equals(Order.Status.CREATED)) {
                throw new DomainException("Invalid order state " + order);
            }

            long requestedQuantity = order.getQuantity();

            if (requestedQuantity > getQuantity()) {
                order.cancel();
                throw new StockUnavailableException(String.format(
                        "The requested quantity %d is greater than the available quantity %d, for product %s",
                        requestedQuantity,
                        getQuantity(),
                        getProductCode()
                ));
            }

            this.quantity -= requestedQuantity;
            order.process();
        }
    }

    @Override
    public String toString() {
        return String.format("Stock item: code = %s, quantity = %s", productCode, quantity);
    }

    @Override
    public String getId() {
        return productCode;
    }

    @Override
    public void setId(String s) {
        throw new UnsupportedOperationException();
    }
}
