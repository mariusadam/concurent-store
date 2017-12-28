package com.ubb.ppd.lab4.server.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marius Adam
 */
public class CompositeEndpoint implements EndpointInterface {
    private Collection<EndpointInterface> endpoints       = new ArrayList<>();
    private boolean                       started         = false;
    private ExecutorService               executorService = null;

    public void addEndpoint(EndpointInterface endpoint) {
        endpoints.add(endpoint);
    }

    @Override
    public synchronized void close() throws Exception {
        if (!started) {
            throw new RuntimeException("The endpoint was not started.");
        }

        executorService.shutdown();

        for (EndpointInterface endpoint : endpoints) {
            endpoint.close();
        }
    }

    @Override
    public synchronized void expose() {
        if (started) {
            throw new UnsupportedOperationException("Endpoints started already.");
        }

        endpoints = Collections.unmodifiableCollection(endpoints);
        executorService = Executors.newFixedThreadPool(endpoints.size());
        endpoints.forEach(
                endpoint -> CompletableFuture.runAsync(endpoint::expose, executorService)
        );
        started = true;
    }
}
