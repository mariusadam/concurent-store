package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.model.StockItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class ProductCodesEndpoint extends Endpoint {
    private Store  store;
    private Logger logger;

    public ProductCodesEndpoint(int port, Store store, Logger logger) throws IOException {
        super(port, logger);
        this.store = store;
        this.logger = logger;
    }

    @Override
    protected void serve(Socket clientSocket) throws IOException {
        logger.info("Starting to serve product codes.");
        store.getAllProductCodes().forEach(s -> logger.info(s));

        try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {

            Collection<StockItem> items = store.getAvailableStockItems();

            writer.println(Integer.toString(items.size()));
            items.forEach(stockItem -> {
                writer.println(stockItem.getProductCode());
                writer.println(stockItem.getQuantity());
            });
        }
    }
}
