package com.ubb.ppd.lab4.server.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * @author Marius Adam
 */
public abstract class AbstractEndpoint implements Closeable, AutoCloseable, EndpointInterface {
    private ServerSocket serverSock;
    private Logger       logger;

    AbstractEndpoint(int exposedPort, Logger logger) throws IOException {
        serverSock = new ServerSocket(exposedPort);
        this.logger = logger;
    }

    @Override
    public void close() throws IOException {
        serverSock.close();
    }

    @Override
    public void start() {
        logger.info(String.format(
                "Started listening on connections on port %s",
                serverSock.getLocalSocketAddress())
        );

        try {
            while (!serverSock.isClosed()) {
                logger.info("Waiting for connections...");
                Socket socket = serverSock.accept();
                logger.info("Serving " + socket.getInetAddress());
                long start = System.currentTimeMillis();

                serve(socket);

                long end = System.currentTimeMillis();
                logger.info("Done serving " + socket.getInetAddress() + " in " + (end - start) + " millis");
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    abstract protected void serve(Socket clientSocket) throws IOException;
}