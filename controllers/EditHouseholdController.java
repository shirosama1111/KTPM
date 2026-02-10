package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.HouseholdDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.Household;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.List;

public class EditHouseholdController extends BaseController {

    @FXML private TextField tfOwnerId;
    @FXML private TextField tfOwnerCccd;
    @FXML private TextField tfAddress;

    private final ContextMenu suggestionMenu = new ContextMenu();
    private int selectedIndex = -1;

    private Stage stage;
    private Household household;
    private Runnable onEditSuccess;

    /* ================= INIT ================= */

    @FXML
    public void initialize() {
        setupOwnerIdAutoComplete();
        setupOwnerCccdAutoComplete();
        setupLocking();
        setupKeyboardNavigation();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setHousehold(Household h) {
        this.household = h;

        if (h.getHeadCitizenId() != null) {
            tfOwnerId.setText(String.valueOf(h.getHeadCitizenId()));
        }

        tfAddress.setText(h.getAddress());
    }

    public void setOnEditSuccess(Runnable r) {
        this.onEditSuccess = r;
    }

    /* ================= LOCK ID <-> CCCD ================= */

    private void setupLocking() {
        tfOwnerId.textProperty().addListener((obs, o, n) ->
                tfOwnerCccd.setDisable(!n.isBlank()));

        tfOwnerCccd.textProperty().addListener((obs, o, n) ->
                tfOwnerId.setDisable(!n.isBlank()));
    }

    /* ================= AUTOCOMPLETE ID ================= */

    private void setupOwnerIdAutoComplete() {

        tfOwnerId.textProperty().addListener((obs, o, n) -> {
            selectedIndex = -1;

            if (n.isBlank()) {
                suggestionMenu.hide();
                return;
            }

            try {
                List<Citizen> list = CitizenDao.searchByIdPrefix(n);
                if (list.isEmpty()) {
                    suggestionMenu.hide();
                    return;
                }

                buildMenu(list, true);
                if (tfOwnerId.getScene() != null) {
                    suggestionMenu.show(tfOwnerId, Side.BOTTOM, 0, 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /* ================= AUTOCOMPLETE CCCD ================= */

    private void setupOwnerCccdAutoComplete() {

        tfOwnerCccd.textProperty().addListener((obs, o, n) -> {
            selectedIndex = -1;

            if (n.isBlank()) {
                suggestionMenu.hide();
                return;
            }

            try {
                List<Citizen> list = CitizenDao.searchByCccdPrefix(n);
                if (list.isEmpty()) {
                    suggestionMenu.hide();
                    return;
                }

                buildMenu(list, false);
                if (tfOwnerCccd.getScene() != null) {
                    suggestionMenu.show(tfOwnerCccd, Side.BOTTOM, 0, 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /* ================= BUILD MENU ================= */

    private void buildMenu(List<Citizen> list, boolean byId) {
        suggestionMenu.getItems().clear();

        for (Citizen c : list) {
            MenuItem item = new MenuItem(
                    c.getFullName()
                            + " | CCCD: " + c.getCccd()
                            + " | ID: " + c.getId()
            );

            item.setOnAction(e -> {
                if (byId) {
                    tfOwnerId.setText(String.valueOf(c.getId()));
                    tfOwnerCccd.clear();
                } else {
                    tfOwnerCccd.setText(c.getCccd());
                    tfOwnerId.clear();
                }
                suggestionMenu.hide();
            });

            suggestionMenu.getItems().add(item);
        }
    }

    /* ================= KEYBOARD ↑ ↓ ENTER ================= */

    private void setupKeyboardNavigation() {

        EventHandler<KeyEvent> handler = e -> {

            if (!suggestionMenu.isShowing()) return;

            int size = suggestionMenu.getItems().size();
            if (size == 0) return;

            switch (e.getCode()) {
                case DOWN -> {
                    selectedIndex = (selectedIndex + 1) % size;
                    highlight();
                }
                case UP -> {
                    selectedIndex = (selectedIndex - 1 + size) % size;
                    highlight();
                }
                case ENTER -> {
                    if (selectedIndex >= 0) {
                        suggestionMenu.getItems().get(selectedIndex).fire();
                        selectedIndex = -1;
                    }
                }
                case ESCAPE -> suggestionMenu.hide();
            }
        };

        tfOwnerId.setOnKeyPressed(handler);
        tfOwnerCccd.setOnKeyPressed(handler);
    }

    private void highlight() {
        for (int i = 0; i < suggestionMenu.getItems().size(); i++) {
            MenuItem item = suggestionMenu.getItems().get(i);
            item.setStyle(i == selectedIndex
                    ? "-fx-background-color:#cce5ff;"
                    : "");
        }
    }

    /* ================= SAVE ================= */

    @FXML
    private void handleSave() {
        if (household == null) return;

        if (tfAddress.getText().isBlank()) {
            showWarning("Thiếu thông tin", "Địa chỉ không được để trống!");
            return;
        }

        try {
            Integer newHeadId = null;

            if (!tfOwnerId.getText().isBlank()) {
                int id = Integer.parseInt(tfOwnerId.getText().trim());
                Citizen c = CitizenDao.findById(id);
                if (c == null) {
                    showWarning("Sai ID", "Không tồn tại công dân này!");
                    return;
                }
                newHeadId = id;
            }
            else if (!tfOwnerCccd.getText().isBlank()) {
                Citizen c = CitizenDao.findByCccd(tfOwnerCccd.getText().trim());
                if (c == null) {
                    showWarning("Sai CCCD", "Không tìm thấy công dân!");
                    return;
                }
                newHeadId = c.getId();
            }

            household.setHeadCitizenId(newHeadId);
            household.setAddress(tfAddress.getText().trim());

            HouseholdDao.update(household);

            if (onEditSuccess != null) onEditSuccess.run();
            stage.close();

        } catch (NumberFormatException e) {
            showWarning("Sai dữ liệu", "ID phải là số!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể cập nhật hộ khẩu!");
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }
}