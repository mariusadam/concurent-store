package com.ubb.ppd.lab4.server;

import com.ubb.ppd.lab4.server.domain.CommandPlacer;
import com.ubb.ppd.lab4.server.domain.StockChecker;
import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.net.Endpoint;
import com.ubb.ppd.lab4.server.net.ProcessOrderEndpoint;
import com.ubb.ppd.lab4.server.net.ProductCodesEndpoint;
import sun.applet.Main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * @author Marius Adam
 */
public class BootstrapServer {
    public static final  String  BASE_DIR               = "/home/marius/IdeaProjects/ppd-lab4-owned/";
    public static final  String  LOG_FILE               = BASE_DIR + "store.log";
    public static final  String  STORE_FILE             = BASE_DIR + "store.txt.log";
    public static final  int     PRODUCT_CODES_PORT     = 4545;
    public static final  int     PROCESS_ORDER_ENDPOINT = 5454;
    public static final  boolean CONSOLE_LOG_ENABLED    = true;
    public static final  boolean FILE_LOG_ENABLED       = true;
    private static final Logger  logger                 = createLogger();

    public static void main(String[] args) throws InterruptedException, IOException {

        CountDownLatch emptyStoreLatch   = new CountDownLatch(1);
        OutputStream   storeOutputStream = new FileOutputStream(STORE_FILE);
        Store          store             = new Store(1, logger);
        StockChecker   checker           = new StockChecker(store, storeOutputStream, emptyStoreLatch);
        Runnable       storeDumper       = () -> store.dump(storeOutputStream);
        Runnable       commandPlacer     = new CommandPlacer(store, logger);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(checker, 0, 5, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(storeDumper, 0, 15, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(commandPlacer, 0, 10, TimeUnit.MILLISECONDS);

        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        List<Endpoint>  endpoints  = new ArrayList<>();
        try {
            logger.info("Creating endpoints");
            endpoints.add(new ProductCodesEndpoint(PRODUCT_CODES_PORT, store, logger));
            endpoints.add(new ProcessOrderEndpoint(PROCESS_ORDER_ENDPOINT, store, logger));

            logger.info("Starting endpoints");
            endpoints.forEach(endpoint -> threadPool.submit(endpoint::start));

            logger.info("Awaiting for the store to be empty...");
            emptyStoreLatch.await();
            logger.info("Store is empty. Closing down endpoints");
        } catch (Exception e) {
            logger.severe("Exception occurred " + e.getMessage());
            logger.severe("Freeing resources and shutting down");
        } finally {
            storeOutputStream.close();
            endpoints.forEach(BootstrapServer::closeEndpoint);
            scheduledExecutorService.shutdown();
            threadPool.shutdown();
            logger.info("Endpoints closed. Exiting now...");
        }
    }

    private static void closeEndpoint(Endpoint endpoint) {
        try {
            logger.info("Attempting to close " + endpoint.getClass().getSimpleName());
            endpoint.close();
            logger.info(endpoint.getClass().getSimpleName() + " closed");
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    private static Logger createLogger() {
        try {
            Logger        logger   = Logger.getLogger(Main.class.getName());
            List<Handler> handlers = new ArrayList<>();

            if (CONSOLE_LOG_ENABLED) {
                handlers.add(new ConsoleHandler());
            }

            if (FILE_LOG_ENABLED) {
                handlers.add(new FileHandler(LOG_FILE));
            }

            handlers.forEach(handler -> {
                        handler.setFormatter(new SimpleFormatter());
                        logger.addHandler(handler);
                    }
            );

            logger.setUseParentHandlers(false);

            return logger;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
