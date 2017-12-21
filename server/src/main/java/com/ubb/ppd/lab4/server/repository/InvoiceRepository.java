package com.ubb.ppd.lab4.server.repository;

import com.ubb.ppd.lab4.server.model.Invoice;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marius Adam
 */
public class InvoiceRepository extends AbstractRepository<Long, Invoice> {
    private final Object mutex     = new Object();
    private       Long   idCounter = 0L;

    public InvoiceRepository() {
        super(ConcurrentHashMap::new);
    }

    @Override
    protected void beforeSave(Invoice item) {
        synchronized (mutex) {
            if (item.getId() == null) {
                item.setId(++idCounter);
            }
        }
    }
}
