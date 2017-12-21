package com.ubb.ppd.lab4.server.repository;

import com.ubb.ppd.lab4.server.model.Order;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marius Adam
 */
public class OrderRepository extends AbstractRepository<Long, Order> {
    private final Object mutex     = new Object();
    private       Long   idCounter = 0L;

    public OrderRepository() {
        super(ConcurrentHashMap::new);
    }

    @Override
    protected void beforeSave(Order item) {
        synchronized (mutex) {
            if (item.getId() == null) {
                item.setId(++idCounter);
            }
        }
    }

    public long count(Order.Status status) {
        return streamAll()
                .filter(order -> order.getStatus().equals(status))
                .count();
    }
}
