package com.ubb.ppd.lab4.server.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.ubb.ppd.lab4.server.net.RequestResponseEndpoint.Mode.ASYNCHRONOUS;
import static com.ubb.ppd.lab4.server.net.RequestResponseEndpoint.Mode.SYNCHRONOUS;

/**
 * @author Marius Adam
 */
public abstract class RequestResponseEndpoint implements Closeable, EndpointInterface {
    protected final Logger          logger;
    private final   ServerSocket    serverSock;
    private final   Mode            servingMode;
    private final   Handler         handler;
    private final   ExecutorService asyncServerExecutor;

    RequestResponseEndpoint(int exposedPort, Mode servingMode, Logger logger) throws IOException {
        serverSock = new ServerSocket(exposedPort);
        this.logger = logger;
        this.servingMode = servingMode;

        if (servingMode == ASYNCHRONOUS) {
            handler = this::serveAsynchronously;
            asyncServerExecutor = Executors.newFixedThreadPool(10);
        } else {
            handler = this::serveSynchronously;
            asyncServerExecutor = null;
        }
    }

    @Override
    public void close() throws IOException {
        serverSock.close();

        if (servingMode != SYNCHRONOUS) {
            asyncServerExecutor.shutdown();
        }
    }

    @Override
    public void expose() {
        logger.info(format(
                "%s started listening for connections on port %d",
                getClass().getName(),
                serverSock.getLocalPort())
        );

        try {
            while (!serverSock.isClosed()) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                logger.info(format("Waiting for connections..."));
                Socket socket = serverSock.accept();
                logger.info(format("Connection established to %s", socket));
                long start = System.currentTimeMillis();

                handler.handle(socket);

                long end = System.currentTimeMillis();
                logger.info(format(format("Done serving (%s) %s in %d millis", servingMode, socket, end - start)));
            }
        } catch (InterruptedException e) {
            logger.warning(format("Endpoint terminated."));
        } catch (Exception e) {
            logger.severe(format("Failed while being exposed: %s", e.getMessage()));
        }
    }

    private void serveSynchronously(Socket clientSocket) {
        try (
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8")
                );
                InputStreamReader reader = new InputStreamReader(
                        clientSocket.getInputStream(), "UTF-8"
                );
                Scanner scanner = new Scanner(reader)
        ) {
            doServe(scanner, writer);
            writer.flush();
        } catch (Throwable t) {
            logger.severe(format("Failed to serve client %s. Reason %s", clientSocket, t.getMessage()));
        }
    }

    private void serveAsynchronously(Socket clientSocket) {

        CompletableFuture
                .runAsync(() -> serveSynchronously(clientSocket), asyncServerExecutor)
                .thenAccept(aVoid -> logger.info(format("Finished serving asynchronously %s", clientSocket)))
                .handle((aVoid, throwable) -> {
                    logger.severe(format(
                            "Failed while serving asynchronously client %s: %s",
                            clientSocket,
                            throwable.getMessage()
                    ));
                    return null;
                });
    }

    abstract protected void doServe(Scanner input, PrintWriter writer);

    /**
     * Prepend the class name to the given message
     *
     * @param format String format
     * @param args   Arguments referenced by the format specifiers
     * @return The formatted string
     */
    private String format(String format, Object... args) {
        return String.format(
                "[%s] - %s",
                getClass().getName(),
                String.format(format, args)
        );
    }

    public enum Mode {
        ASYNCHRONOUS, SYNCHRONOUS
    }

    private interface Handler {
        void handle(Socket socket);
    }
}
