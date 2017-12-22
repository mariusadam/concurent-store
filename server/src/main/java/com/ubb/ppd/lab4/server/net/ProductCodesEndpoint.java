package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
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

            Collection<String> codes = store.getAllProductCodes();

            writer.println(Integer.toString(Store.STOCK_MAX_QUANTITY));
            writer.println(Integer.toString(codes.size()));
            codes.forEach(writer::println);
        }
    }
}
