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

            Integer  size     = Integer.parseInt(scanner.nextLine());
            Response response = new Response();
            for (int i = 0; i < size; ++i) {
                response.responseItems.add(new ResponseItem(
                        scanner.nextLine(),
                        Integer.parseInt(scanner.nextLine())
                ));
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Response {
        List<ResponseItem> responseItems = new ArrayList<>();

        public List<ResponseItem> getResponseItems() {
            return responseItems;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "responseItems=\n" + responseItems +
                    "\n}";
        }
    }

    public static class ResponseItem {
        private String productCode;
        private int    quantity;

        public ResponseItem(String productCode, int quantity) {
            this.productCode = productCode;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "\n\tResponseItem{" +
                    "productCode='" + productCode + '\'' +
                    ", quantity=" + quantity +
                    "}";
        }

        public String getProductCode() {
            return productCode;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
