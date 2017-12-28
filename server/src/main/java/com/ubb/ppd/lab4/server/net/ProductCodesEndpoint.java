package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.model.StockItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class ProductCodesEndpoint extends RequestResponseEndpoint {
    private Store store;

    public ProductCodesEndpoint(int exposedPort, Mode servingMode, Logger logger, Store store) throws IOException {
        super(exposedPort, servingMode, logger);
        this.store = store;
    }

    @Override
    protected void doServe(Scanner input, PrintWriter writer) {
        logger.info("Starting to serve product codes.");

        Collection<StockItem> items = store.getAvailableStockItems();
        logger.info("Product codes : " + items);

        writer.println(Integer.toString(items.size()));
        items.forEach(stockItem -> {
            writer.println(stockItem.getProductCode());
            writer.println(stockItem.getQuantity());
        });

        logger.info("Finished serving product codes.");
    }
}
