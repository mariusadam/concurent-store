package com.ubb.ppd.lab4.server.repository;

import com.ubb.ppd.lab4.server.domain.exception.DomainException;
import com.ubb.ppd.lab4.server.model.StockItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marius Adam
 */
public class StockItemRepository extends AbstractRepository<String, StockItem> {
    public StockItemRepository() {
        super(HashMap::new);
    }

    public StockItem findByProductCode(String productCode) {
        StockItem stockItem = findOne(stockItem1 -> stockItem1.getProductCode().equals(productCode));

        if (stockItem == null) {
            throw new DomainException("Could not find stock item for product " + productCode);
        }

        return stockItem;
    }

    public Collection<StockItem> getAvailable() {
        return streamAll()
                .filter(stockItem -> stockItem.getQuantity() > 0)
                .collect(Collectors.toList());
    }

    public long availableProductsCount() {
        return streamAll()
                .filter(stockItem -> stockItem.getQuantity() > 0)
                .count();
    }
}
