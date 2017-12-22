package com.ubb.ppd.lab4.client;

import com.ubb.ppd.lab4.client.net.NotificationClient;
import com.ubb.ppd.lab4.client.net.ProcessOrderClient;
import com.ubb.ppd.lab4.client.net.ProductCodesClient;

import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;

/**
 * @author Marius Adam
 */
public class BootstrapClient {
    public static final String  SERVER_HOST        = "localhost";
    public static final Integer PRODUCT_CODES_PORT = 4545;
    public static final Integer PROCESS_ORDER_PORT = 5454;
    public static final Integer NOTIFICATION_PORT  = 5555;
    public static final String  CLIENT_USAGE       = "Usage: java -jar /path/to/jar <number-of-orders>";
    public static final int     THREADS_COUNT      = 5;


    public static void main(String[] args) throws InterruptedException {
        ProductCodesClient productCodesClient = new ProductCodesClient(
                () -> new Socket(SERVER_HOST, PRODUCT_CODES_PORT)
        );
        NotificationClient notificationClient = new NotificationClient(
                () -> new Socket(SERVER_HOST, NOTIFICATION_PORT)
        );

        notificationClient.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println("Received an update from server.");
            }
        });

        System.out.println(productCodesClient.execute());

        ProcessOrderClient processOrderClient = new ProcessOrderClient(
                () -> new Socket(SERVER_HOST, PROCESS_ORDER_PORT)
        );

        CountDownLatch finishedRequests = new CountDownLatch(1);

        System.out.println(
                processOrderClient.execute(new ProcessOrderClient.Request(
                    "B000IAPR0U", 2
                ))
        );

        finishedRequests.await();
//        ProductCodesClient.Response codesResponse = productCodesClient.execute();
//
//        ExecutorService threadPool = Executors.newFixedThreadPool(THREADS_COUNT);
//        try (Scanner scanner = new Scanner(System.in)) {
//            System.out.print("Enter the number of orders to send -> ");
//            Integer ordersCount = scanner.nextInt();
//            if (ordersCount < 1 || ordersCount > 10000) {
//                throw new RuntimeException("The number of orders has to be between 0 and 10000");
//            }
//
//
//            for (int i = 0; i < ordersCount; ++i) {
//                threadPool.submit(() -> {
//                    finishedRequests.countDown();
//                    Random random = new SecureRandom();
//                    String code = codesResponse.getProductCodes().get(
//                            random.nextInt(codesResponse.getProductCodes().size())
//                    );
//                    Integer quantity = random.nextInt(codesResponse.getMaxQuantity());
//
//                    System.out.println("Sending order for product code " + code);
//                    System.out.println(processOrderClient.execute(
//                            new ProcessOrderClient.Request(code, quantity)
//                    ));
//                });
//            }
//
//            finishedRequests.await();
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//            System.err.println(CLIENT_USAGE);
//        } finally {
//            threadPool.shutdown();
//        }
    }
}
