package com.ubb.ppd.lab4.client.net;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author Marius Adam
 */
public class ProcessOrderClient extends AbstractClient<ProcessOrderClient.Request, ProcessOrderClient.Response> {
    public ProcessOrderClient(SocketFactory socketFactory) {
        super(socketFactory);
    }

    @Override
    protected Response doExecute(Request request, Scanner input, PrintWriter writer) {
        writer.println(request.productCode);
        writer.println(request.quantity);
        writer.flush();

        return new Response(
                input.nextLine(),
                input.nextLine()
        );
    }

    public static class Response {
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
