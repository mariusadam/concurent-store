package com.ubb.ppd.lab4.client.net;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Marius Adam
 */
public class ProcessOrderClient {
    private SocketFactory socketFactory;

    public ProcessOrderClient(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public Response execute(Request request) {
        try (Socket socket = socketFactory.createSocket();
             PrintStream printStream = new PrintStream(socket.getOutputStream());
             Scanner scanner = new Scanner(socket.getInputStream())) {

            printStream.println(request.productCode);
            printStream.println(request.quantity);

            return new Response(
                    scanner.nextLine(),
                    scanner.nextLine()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class Response {
        private String[] messages;

        Response(String... messages) {
            this.messages = messages;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "messages=\n\t" + String.join("\n\t", messages) +
                    "\n}";
        }
    }

    public static class Request {
        private String productCode;
        private long   quantity;

        public Request(String productCode, long quantity) {
            this.productCode = productCode;
            this.quantity = quantity;
        }
    }
}
