package com.ubb.ppd.lab4.server.net;

import java.io.IOException;

/**
 * @author Marius Adam
 */
public interface EndpointInterface {
    void close() throws IOException;

    void start();
}
