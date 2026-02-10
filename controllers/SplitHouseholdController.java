package com.example.demo4.controllers;

import com.example.demo4.Database;
import com.example.demo4.Session;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.HouseholdDao;
import com.example.demo4.dao.HouseholdChangeDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.Household;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SplitHouseholdController extends BaseController {

    @FXML private Label lblOriginalHousehold;
    @FXML private TableView<Citizen> tableAllMembers;
    @FXML private TableView<Citizen> tableNewMembers;

    @FXML private TableColumn<Citizen, String> colNameAll;
    @FXML private TableColumn<Citizen, String> colRelationAll;
    @FXML private TableColumn<Citizen, String> colCccdAll;

    @FXML private TableColumn<Citizen, String> colNameNew;
    @FXML private TableColumn<Citizen, String> colRelationNew;
    @FXML private TableColumn<Citizen, String> colCccdNew;

    @FXML private TextField tfNewAddress;
    @FXML private TextArea taNote;

    private Stage stage;
    private Household originalHousehold;
    private Runnable onSplitSuccess;

    private final ObservableList<Citizen> allMembers = FXCollections.observableArrayList();
    private final ObservableList<Citizen> newHouseholdMembers = FXCollections.observableArrayList();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOriginalHousehold(Household household) {
        this.originalHousehold = household;
        lblOriginalHousehold.setText("Tách từ hộ: " + household.getHouseholdId() + " - " + household.getAddress());
        loadMembers();
    }

    public void setOnSplitSuccess(Runnable r) {
        this.onSplitSuccess = r;
    }

    @FXML
    public void initialize() {
        colNameAll.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));
        colRelationAll.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getRelation()));
        colCccdAll.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCccd()));

        colNameNew.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));
        colRelationNew.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getRelation()));
        colCccdNew.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCccd()));

        tableAllMembers.setItems(allMembers);
        tableNewMembers.setItems(newHouseholdMembers);

        tableAllMembers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableNewMembers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadMembers() {
        try {
            List<Citizen> members = CitizenDao.findByHouseholdId(originalHousehold.getHouseholdId());
            allMembers.setAll(members);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải danh sách thành viên!");
        }
    }

    @FXML
    private void moveToNewHousehold() {
        ObservableList<Citizen> selected = tableAllMembers.getSelectionModel().getSelectedItems();

        if (selected.isEmpty()) {
            showWarning("Chưa chọn", "Hãy chọn ít nhất một thành viên để chuyển!");
            return;
        }

        for (Citizen c : selected) {
            if (Boolean.TRUE.equals(c.getHouseholder())) {
                boolean confirm = showConfirm(
                        "Xác nhận",
                        "Bạn đang chuyển chủ hộ sang hộ mới. Hộ cũ sẽ không còn chủ hộ. Tiếp tục?"
                );
                if (!confirm) return;
                break;
            }
        }

        newHouseholdMembers.addAll(selected);
        allMembers.removeAll(selected);
    }

    @FXML
    private void moveBackToOriginal() {
        ObservableList<Citizen> selected = tableNewMembers.getSelectionModel().getSelectedItems();

        if (selected.isEmpty()) {
            showWarning("Chưa chọn", "Hãy chọn thành viên để chuyển về!");
            return;
        }

        allMembers.addAll(selected);
        newHouseholdMembers.removeAll(selected);
    }

    @FXML
    private void handleSplit() {
        if (newHouseholdMembers.isEmpty()) {
            showWarning("Lỗi", "Hộ mới phải có ít nhất một thành viên!");
            return;
        }

        if (tfNewAddress.getText().trim().isEmpty()) {
            showWarning("Lỗi", "Địa chỉ hộ mới không được để trống!");
            return;
        }

        boolean confirm = showConfirm(
                "Xác nhận tách hộ",
                String.format(
                        "Tách %d thành viên sang hộ mới?\nĐịa chỉ mới: %s\nHộ cũ còn: %d thành viên",
                        newHouseholdMembers.size(),
                        tfNewAddress.getText().trim(),
                        allMembers.size()
                )
        );
        if (!confirm) return;

        try {
            Integer ownerId = Session.getCurrentUserId();
            if (ownerId == null) {
                showWarning("Lỗi", "Chưa đăng nhập!");
                return;
            }

            // Xác định chủ hộ mới (nếu không có, chọn người đầu tiên)
            Citizen newHead = newHouseholdMembers.stream()
                    .filter(Citizen::getHouseholder)
                    .findFirst()
                    .orElse(newHouseholdMembers.get(0));

            // Tạo hộ mới với head tạm thời = null
            Household newHousehold = new Household(0, null, tfNewAddress.getText().trim(), ownerId);

            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);

                // 1. Thêm hộ mới
                HouseholdDao.insert(newHousehold); // insert không nhận Connection
                int newHouseholdId = HouseholdDao.getNextHouseholdId(); // lấy id mới

                // 2. Chuyển tất cả công dân sang hộ mới
                for (Citizen c : newHouseholdMembers) {
                    // Cập nhật household_id và is_householder
                    c.setHouseholdId(newHouseholdId);
                    c.setHouseholder(c.getId() == newHead.getId());
                    if (c.getId() == newHead.getId()) c.setRelation(null);
                    else if (c.getRelation() == null) c.setRelation("Thành viên"); // default

                    CitizenDao.update(c); // update toàn bộ thông tin
                }

                // 3. Cập nhật chủ hộ cũ nếu cần
                if (!allMembers.isEmpty()) {
                    boolean oldHeadExists = allMembers.stream().anyMatch(Citizen::getHouseholder);
                    if (!oldHeadExists) {
                        Citizen newOriginalHead = allMembers.get(0);
                        newOriginalHead.setHouseholder(true);
                        newOriginalHead.setRelation(null);
                        CitizenDao.update(newOriginalHead);
                        originalHousehold.setHeadCitizenId(newOriginalHead.getId());
                    }
                    HouseholdDao.update(originalHousehold);
                } else {
                    // Nếu hộ cũ hết người thì head = null
                    originalHousehold.setHeadCitizenId(null);
                    HouseholdDao.update(originalHousehold);
                }

                // 4. Ghi lịch sử tách hộ
                String note = String.format(
                        "Tách hộ: %d thành viên chuyển sang hộ %d. %s",
                        newHouseholdMembers.size(),
                        newHouseholdId,
                        taNote.getText().trim()
                );
                HouseholdChangeDao.insert(originalHousehold.getHouseholdId(),
                        java.time.LocalDate.now().toString(), note);

                conn.commit();

                // 5. Cập nhật ObservableList để TableView hiển thị đúng
                allMembers.removeAll(newHouseholdMembers);
                // newHouseholdMembers đã có sẵn

                showInfo("Thành công",
                        String.format("Đã tách hộ thành công!\nHộ mới: %d", newHouseholdId));

                if (onSplitSuccess != null) onSplitSuccess.run();
                close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Lỗi", "Không thể tách hộ: " + ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tách hộ: " + e.getMessage());
        }
    }


    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        if (stage != null) stage.close();
    }
}
