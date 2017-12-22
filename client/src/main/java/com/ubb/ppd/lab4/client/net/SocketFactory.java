package com.ubb.ppd.lab4.client.net;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Marius Adam
 */
public interface SocketFactory {
    Socket createSocket() throws IOException;
}
