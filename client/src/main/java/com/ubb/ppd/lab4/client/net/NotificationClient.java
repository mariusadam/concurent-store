package com.ubb.ppd.lab4.client.net;

import com.ubb.ppd.lab4.client.Event;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marius Adam
 */
public class NotificationClient extends Observable implements AutoCloseable {
    private Socket socket;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ExecutorService notifyService   = Executors.newSingleThreadExecutor();
    private CompletableFuture<Void> previousUpdate;

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
            while (!socket.isClosed() && socket.isConnected()) {
                try {
                    System.out.println("Waiting for events");
                    int b = socket.getInputStream().read();
                    if (previousUpdate != null && !previousUpdate.isDone()) {
                        previousUpdate.cancel(true);
                        System.out.println("Stopped previous update");
                    }
                    previousUpdate = CompletableFuture.runAsync(() -> {
                        System.out.println("Event received, notifying obs");
                        setChanged();
                        notifyObservers(new Event(b));
                    }, notifyService);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        socket.close();
        executorService.shutdown();
        notifyService.shutdown();
    }
}
