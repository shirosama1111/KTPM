package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.Session;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.HouseholdDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.Household;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class HouseholdCustomerController extends BaseController {

    @FXML private TableView<Citizen> table;

    @FXML private TableColumn<Citizen, String> colName;
    @FXML private TableColumn<Citizen, String> colCccd;
    @FXML private TableColumn<Citizen, String> colDob;
    @FXML private TableColumn<Citizen, String> colRelation;
    @FXML private TableColumn<Citizen, String> colJob;

    @FXML
    public void initialize() {

        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFullName())
        );

        colCccd.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCccd())
        );

        colDob.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getDob() == null ? "" : c.getValue().getDob().toString()
                )
        );

        colRelation.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRelation())
        );

        colJob.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getJob())
        );

        loadMyCitizens();
    }

    private void loadMyCitizens() {
        try {
            Integer userId = Session.getCurrentUserId();
            if (userId == null) {
                showWarning("Lỗi", "Bạn chưa đăng nhập!");
                return;
            }

            List<Household> households = HouseholdDao.findByOwner(userId);
            List<Citizen> allCitizens = new ArrayList<>();

            for (Household h : households) {
                allCitizens.addAll(
                        CitizenDao.findByHouseholdId(h.getHouseholdId())
                );
            }

            table.getItems().setAll(allCitizens);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải nhân khẩu");
        }
    }

    @FXML
    private void backToMenu() throws Exception {
        Main.showMenu();
    }
}