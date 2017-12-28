package com.ubb.ppd.lab4.server.net;

import com.ubb.ppd.lab4.server.domain.Store;

import java.io.IOException;
import java.net.ServerSocket;
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
public class NotificationEndpoint implements Observer, EndpointInterface {
    private final Logger logger;
    private final List<Socket>    connectedClients     = new ArrayList<>();
    private final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService startExecutor        = Executors.newSingleThreadExecutor();
    private final ServerSocket serverSocket;

    public NotificationEndpoint(int exposedPort, Store store, Logger logger) throws IOException {
        this.logger = logger;
        this.serverSocket = new ServerSocket(exposedPort);
        store.addObserver(this);
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
        notificationExecutor.shutdown();
        startExecutor.shutdown();
    }

    @Override
    public void expose() {
        startExecutor.submit(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    connectedClients.add(serverSocket.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
