package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public class NotificationEndpoint extends Endpoint implements Observer {
    private Logger          logger;
    private List<Socket>    connectedClients;
    private ExecutorService notificationExecutor;

    public NotificationEndpoint(int port, Store store, Logger logger) throws IOException {
        super(port, logger);
        this.logger = logger;
        this.connectedClients = new ArrayList<>();
        this.notificationExecutor = Executors.newSingleThreadExecutor();
        store.addObserver(this);
    }

    @Override
    protected void serve(Socket clientSocket) throws IOException {
        connectedClients.add(clientSocket);
    }

    @Override
    public void close() throws IOException {
        super.close();
        notificationExecutor.shutdown();
    }

    @Override
    public void update(Observable o, Object arg) {
        logger.info("Starting to notify " + connectedClients.size() + " connected clients");
        // do not block the main thread
        notificationExecutor.submit(() -> connectedClients.forEach(socket -> {
            try {
                logger.info("Notifying client " + socket.getInetAddress());
                socket.getOutputStream().write(1);
            } catch (IOException e) {
                logger.severe(e.getMessage());
                e.printStackTrace();
            }
        }));
    }
}
