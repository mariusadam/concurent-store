package com.ubb.ppd.lab4.client.net;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Marius Adam
 */
public class ProductCodesClient {
    private SocketFactory socketFactory;

    public ProductCodesClient(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public Response execute() {
        try (Socket socket = socketFactory.createSocket();
             Scanner scanner = new Scanner(socket.getInputStream())) {

            Integer      maxQuantity = Integer.parseInt(scanner.nextLine());
            Integer      size        = Integer.parseInt(scanner.nextLine());
            List<String> codes       = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                codes.add(scanner.nextLine());
            }
            return new Response(codes, maxQuantity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Response {
        private List<String> productCodes;
        private int          maxQuantity;

        public Response(List<String> productCodes, int maxQuantity) {
            this.productCodes = productCodes;
            this.maxQuantity = maxQuantity;
        }

        public List<String> getProductCodes() {
            return productCodes;
        }

        public int getMaxQuantity() {
            return maxQuantity;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "productCodes=" + productCodes +
                    ", maxQuantity=" + maxQuantity +
                    '}';
        }
    }
}
