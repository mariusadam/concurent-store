package com.ubb.ppd.lab4.server.repository;

import com.ubb.ppd.lab4.server.domain.exception.ProductNotFoundException;
import com.ubb.ppd.lab4.server.model.Product;

import java.util.HashMap;

/**
 * @author Marius Adam
 */
public class ProductRepository extends AbstractRepository<String, Product> {

    public ProductRepository() {
        super(HashMap::new);
    }

    public Product findByCode(String productCode) throws ProductNotFoundException {
        Product p = findOne(product -> product.getCode().equals(productCode));
        if (p == null) {
            throw new ProductNotFoundException("Could not find product with code " + productCode);
        }

        return p;
    }
}
