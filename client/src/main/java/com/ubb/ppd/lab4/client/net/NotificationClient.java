package com.ubb.ppd.lab4.client.net;

import com.ubb.ppd.lab4.client.Event;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marius Adam
 */
public class NotificationClient extends Observable implements AutoCloseable {
    private Socket socket;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NotificationClient(SocketFactory socketFactory) {
        try {
            this.socket = socketFactory.createSocket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        awaitUpdatesAsync();
    }

    private void awaitUpdatesAsync() {
        executorService.submit(() -> {
            while (!socket.isClosed()) {
                try {
                    int b = socket.getInputStream().read();
                    notifyObservers(new Event(b));
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        socket.close();
        executorService.shutdown();
    }
}
