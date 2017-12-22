package com.ubb.ppd.lab4.client.gui;

import com.ubb.ppd.lab4.client.net.ProcessOrderClient;
import com.ubb.ppd.lab4.client.net.ProductCodesClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.*;

import static com.ubb.ppd.lab4.client.BootstrapClient.THREADS_COUNT;

/**
 * @author Marius Adam
 */
public class WindowController implements Observer, Initializable {
    private final ExecutorService          wrapper                     = Executors.newFixedThreadPool(THREADS_COUNT);
    private final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
    @FXML
    private TableColumn<ProductCodesClient.ResponseItem, Integer> stocCol;
    @FXML
    private TableView<ProductCodesClient.ResponseItem>            productsTableView;
    @FXML
    private TextArea                                              textareaField;
    @FXML
    private TextField                                             ordersCountField;
    @FXML
    private TextField                                             maxQuantityField;
    private ObservableList<ProductCodesClient.ResponseItem>       responseItems;
    @FXML
    private TableColumn<ProductCodesClient.ResponseItem, String>  productCodeCol;
    private ProductCodesClient                                    productCodesClient;
    private ProcessOrderClient                                    processOrderClient;

    @FXML
    void onSendButton(ActionEvent event) {
        log("Clicked send.");
        wrapper.submit(() -> {
            createOrders();
            return null;
        });
    }

    @FXML
    void hack(ActionEvent event) {
        log("Updating the table");
        updateTable();
    }

    private void createOrders() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREADS_COUNT);
        log("Created thread pool. Preparing to send orders.");
        try {
            Integer ordersCount = Integer.parseInt(ordersCountField.getText());
            Integer maxQuantity = Integer.parseInt(maxQuantityField.getText());

            if (ordersCount < 1 || ordersCount > 10000) {
                throw new RuntimeException("The number of orders has to be between 0 and 10000");
            }

            CountDownLatch finishedRequests = new CountDownLatch(ordersCount);
            for (int i = 0; i < ordersCount; ++i) {
                log("Creating order #" + i);
                threadPool.submit(() -> {
                    finishedRequests.countDown();
                    Random  random   = new SecureRandom();
                    String  code     = responseItems.get(random.nextInt(responseItems.size())).getProductCode();
                    Integer quantity = random.nextInt(maxQuantity);

                    log("Sending order for product code " + code);
                    log(processOrderClient.execute(
                            new ProcessOrderClient.Request(code, quantity)
                    ).toString());
                });
            }

            finishedRequests.await();
        } catch (Exception e) {
            log(e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private void log(String message) {
        Platform.runLater(() -> textareaField.appendText(message + System.lineSeparator()));
    }

    @Override
    public void update(Observable o, Object arg) {
        updateTable();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productCodeCol.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        stocCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        responseItems = FXCollections.observableArrayList();
        productsTableView.setItems(responseItems);
    }

    public void setProductCodesClient(ProductCodesClient productCodesClient) {
        this.productCodesClient = productCodesClient;
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this::updateTable, 0, 1, TimeUnit.SECONDS);
    }

    private void updateTable() {
        ProductCodesClient.Response response = productCodesClient.execute();
//            log("Received " + response);
        Platform.runLater(() -> {
            responseItems.clear();
            responseItems.addAll(response.getResponseItems());
        });
    }

    public void setProcessOrderClient(ProcessOrderClient processOrderClient) {
        this.processOrderClient = processOrderClient;
    }
}
