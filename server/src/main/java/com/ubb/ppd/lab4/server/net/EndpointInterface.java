package com.ubb.ppd.lab4.server.net;

/**
 * @author Marius Adam
 */
public interface EndpointInterface extends AutoCloseable{
    /**
     * Expose the endpoint's functionality
     */
    void expose();
}
