package com.ubb.ppd.lab4.server.model;

import com.ubb.ppd.lab4.server.util.Money;

/**
 * @author Marius Adam
 */
public class Product implements Identifiable<String> {
    private String code;
    private String name;
    private Money  price;
    private String unitOfMeasurement;

    public Product(String code, String name, Money price, String unitOfMeasurement) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    @Override
    public String getId() {
        return code;
    }

    @Override
    public void setId(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format(
                "Product: code = %s, price = %s, name = %s",
                getCode(),
                getPrice(),
                getName()
        );
    }
}
