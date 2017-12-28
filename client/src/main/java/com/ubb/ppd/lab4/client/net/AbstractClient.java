package com.ubb.ppd.lab4.client.net;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Marius Adam
 */
public abstract class AbstractClient<Req, Res> {
    private SocketFactory socketFactory;

    public AbstractClient(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public Res execute(Req request) {
        try (
                Socket socket = socketFactory.createSocket();
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8")
                );
                InputStreamReader reader = new InputStreamReader(
                        socket.getInputStream(), "UTF-8"
                );
                Scanner scanner = new Scanner(reader)
        ) {
            return doExecute(request, scanner, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    abstract protected Res doExecute(Req request, Scanner input, PrintWriter writer);
}
