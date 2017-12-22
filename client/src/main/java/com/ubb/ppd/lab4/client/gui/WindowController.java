package com.ubb.ppd.lab4.client.gui;

import com.ubb.ppd.lab4.client.net.ProductCodesClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * @author Marius Adam
 */
public class WindowController implements Observer, Initializable {

    @FXML
    private TableView<ProductCodesClient.ResponseItem> productsTableView;

    @FXML
    private TextField ordersCountField;

    private ProductCodesClient productCodesClient;

    public WindowController(ProductCodesClient productCodesClient) {
        this.productCodesClient = productCodesClient;
    }

    @FXML
    void onSendButton(ActionEvent event) {

    }


    @Override
    public void update(Observable o, Object arg) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productsTableView.getItems().addAll(productCodesClient.execute().getResponseItems());
    }
}
