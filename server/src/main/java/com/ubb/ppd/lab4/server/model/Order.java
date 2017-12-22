package com.ubb.ppd.lab4.server.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Marius Adam
 */
public class Order implements Identifiable<Long>, Serializable {
    private Long   id;
    private String productCode;
    private int    quantity;
    private Date   date;
    private Status status;

    public Order(String productCode, int quantity, long milli) {
        this(productCode, quantity, new Date(milli));
    }

    public Order(String productCode, int quantity, Date date) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.date = date;
        this.status = Status.CREATED;
    }

    public void cancel() {
        this.status = Status.CANCELED;
    }

    public void process() {
        this.status = Status.PROCESSED;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format(
                "Order: id = %d, date = %s, productCode = %s, quantity = %d",
                getId(),
                getDate(),
                getProductCode(),
                getQuantity()
        );
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        CREATED, CANCELED, PROCESSED
    }
}
