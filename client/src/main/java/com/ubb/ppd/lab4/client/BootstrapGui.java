package com.ubb.ppd.lab4.client;

import com.ubb.ppd.lab4.client.gui.WindowController;
import com.ubb.ppd.lab4.client.net.NotificationClient;
import com.ubb.ppd.lab4.client.net.ProcessOrderClient;
import com.ubb.ppd.lab4.client.net.ProductCodesClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import static com.ubb.ppd.lab4.client.BootstrapClient.*;

/**
 * @author Marius Adam
 */
public class BootstrapGui extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(BootstrapGui.class.getClassLoader().getResource("window.fxml"));
        Pane root = loader.load();
        primaryStage.setTitle("Hello World");
        WindowController windowController = loader.getController();


        ProductCodesClient productCodesClient = new ProductCodesClient(
                () -> new Socket(SERVER_HOST, PRODUCT_CODES_PORT)
        );
        NotificationClient notificationClient = new NotificationClient(
                () -> new Socket(SERVER_HOST, NOTIFICATION_PORT)
        );
        ProcessOrderClient processOrderClient = new ProcessOrderClient(
                () -> new Socket(SERVER_HOST, PROCESS_ORDER_PORT)
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                notificationClient.close();
                windowController.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        notificationClient.addObserver(windowController);
        notificationClient.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println("Received an update from server.");
            }
        });
        notificationClient.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println("Another observer Received an update from server.");
            }
        });

        windowController.setProductCodesClient(productCodesClient);
        windowController.setProcessOrderClient(processOrderClient);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
