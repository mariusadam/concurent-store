package com.ubb.ppd.lab4.server.model;

import com.ubb.ppd.lab4.server.util.Money;

/**
 * @author Marius Adam
 */
public class Invoice implements Identifiable<Long> {
    private Long   id;
    private String name;
    private Order  order;
    private Money  total;

    public Invoice(Product product, Order order) {
        this(
                product.getCode() + " - " + product.getName(),
                order,
                product.getPrice().times(order.getQuantity())
        );
    }

    public Invoice(String name, Order order, Money total) {
        this.name = name;
        this.order = order;
        this.total = total;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Order getOrder() {
        return order;
    }

    public Money getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return String.format(
                "Invoice: id = %d, name = %s, order = [%s], total = %s",
                getId(),
                getName(),
                getOrder(),
                getTotal()
        );
    }
}
