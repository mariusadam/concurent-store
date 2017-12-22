package com.ubb.ppd.lab4.server;

import com.ubb.ppd.lab4.server.domain.CommandPlacer;
import com.ubb.ppd.lab4.server.domain.StockChecker;
import com.ubb.ppd.lab4.server.domain.Store;
import com.ubb.ppd.lab4.server.net.*;
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
    public static final  String  BASE_DIR               = "/home/mariusadam/IdeaProjects/concurent-store/";
    public static final  String  LOG_FILE               = BASE_DIR + "store.log";
    public static final  String  STORE_CHECK_FILE       = BASE_DIR + "store.check.log";
    public static final  String  STORE_DUMP_FILE        = BASE_DIR + "store.dump.log";
    public static final  int     PRODUCT_CODES_PORT     = 4545;
    public static final  int     PROCESS_ORDER_ENDPOINT = 5454;
    public static final  int     NOTIFICATION_ENDPOINT  = 5555;
    public static final  boolean CONSOLE_LOG_ENABLED    = true;
    public static final  boolean FILE_LOG_ENABLED       = true;
    private static final Logger  logger                 = createLogger();

    public static void main(String[] args) throws InterruptedException, IOException {

        CountDownLatch emptyStoreLatch = new CountDownLatch(1);
        Store          store           = new Store(10, logger);
        OutputStream   checkStream     = new FileOutputStream(STORE_CHECK_FILE);
        StockChecker   checker         = new StockChecker(store, checkStream, emptyStoreLatch);
        OutputStream   dumpStream      = new FileOutputStream(STORE_DUMP_FILE);
        Runnable       storeDumper     = () -> store.dump(dumpStream);
        Runnable       commandPlacer   = new CommandPlacer(store, logger);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(checker, 0, 3, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(storeDumper, 0, 15, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(commandPlacer, 0, 10, TimeUnit.MILLISECONDS);

        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        List<EndpointInterface> endpoints = new ArrayList<>();
        try {
            logger.info("Creating endpoints");
            endpoints.add(new ProductCodesEndpoint(PRODUCT_CODES_PORT, store, logger));
            endpoints.add(new ProcessOrderEndpoint(PROCESS_ORDER_ENDPOINT, store, logger));
            endpoints.add(new NotificationEndpoint(NOTIFICATION_ENDPOINT, store, logger));


            logger.info("Starting endpoints");
            endpoints.forEach(endpoint -> threadPool.submit(endpoint::start));

            logger.info("Awaiting for the store to be empty...");
            emptyStoreLatch.await();
            logger.info("Store is empty. Closing down endpoints");
        } catch (Exception e) {
            logger.severe("Exception occurred " + e.getMessage());
            logger.severe("Freeing resources and shutting down");
        } finally {
            dumpStream.close();
            checkStream.close();
            endpoints.forEach(BootstrapServer::closeEndpoint);
            scheduledExecutorService.shutdown();
            threadPool.shutdown();
            logger.info("Endpoints closed. Exiting now...");
        }
    }

    private static void closeEndpoint(EndpointInterface endpoint) {
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
