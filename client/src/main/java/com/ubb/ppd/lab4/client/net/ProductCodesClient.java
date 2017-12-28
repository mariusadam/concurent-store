package com.ubb.ppd.lab4.client.net;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Marius Adam
 */
public class ProductCodesClient extends AbstractClient<Void, ProductCodesClient.Response> {
    public ProductCodesClient(SocketFactory socketFactory) {
        super(socketFactory);
    }

    public Response execute() {
        return super.execute(null);
    }

    @Override
    protected Response doExecute(Void request, Scanner input, PrintWriter writer) {
        System.out.println("Requesting product codes to server");

        Integer size = Integer.parseInt(input.nextLine());
        System.out.println("Size " + size);
        Response response = new Response();
        for (int i = 0; i < size; ++i) {
            response.responseItems.add(new ResponseItem(
                    input.nextLine(),
                    Integer.parseInt(input.nextLine())
            ));
        }

        return response;
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
