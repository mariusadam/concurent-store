package com.ubb.ppd.lab4.server.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Marius Adam
 */
public class SimpleLogger implements Closeable {
    private final Object   closeLock = new Object();
    private       Executor executor  = Executors.newSingleThreadExecutor();
    private PrintStream[] destinations;

    public SimpleLogger(OutputStream... destinations) {
        this.destinations = new PrintStream[destinations.length];
        System.arraycopy(destinations, 0, destinations, 0, destinations.length);
    }

    @Override
    public void close() throws IOException {
        synchronized (closeLock) {
            for (PrintStream out : destinations) {
                out.close();
            }
        }
    }
}
