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
                    System.out.println("Waiting for events");
                    int b = socket.getInputStream().read();
                    System.out.println("Event received, notifying obs");
                    setChanged();
                    notifyObservers(new Event(b));
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
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
