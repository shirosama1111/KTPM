package com.example.demo4.controllers;

import com.example.demo4.dao.AssetDao;
import com.example.demo4.models.BookingAsset;
import com.example.demo4.models.assets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AssetsController extends BaseController {

    @FXML private TableView<assets> tblAssets;
    @FXML private TableColumn<assets,String> colName;
    @FXML private TableColumn<assets,String> colType;
    @FXML private TableColumn<assets,String> colStatus;
    @FXML private TableColumn<assets,Integer> colQuantity;
    @FXML private TableColumn<assets,Void> colActions;

    @FXML private TextField txtName;
    @FXML private TextField txtType;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblMessage;

    private final ObservableList<assets> assetList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        cbStatus.setItems(FXCollections.observableArrayList(
                "Tốt", "Hư hỏng", "Đang sử dụng"
        ));

        tblAssets.setItems(assetList);
        addButtonToTable();
        refresh();
    }

    private void refresh() {
        assetList.clear();
        try {
            assetList.addAll(AssetDao.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được tài sản!");
        }
    }

    @FXML
    private void openReturnAsset() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/demo4/return_asset.fxml"
                    )
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Trả tài sản");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được màn hình trả tài sản!");
        }
    }

    @FXML
    public void onAddAsset() {
        if (!requireAdmin()) return;

        Integer quantity = parseInt(txtQuantity.getText());
        if (quantity == null) return;

        if (txtName.getText().isBlank()
                || txtType.getText().isBlank()
                || cbStatus.getValue() == null) {
            showWarning("Thiếu thông tin", "Nhập đầy đủ thông tin!");
            return;
        }

        try {
            AssetDao.insert(new assets(
                    0,
                    txtName.getText().trim(),
                    txtType.getText().trim(),
                    quantity,
                    cbStatus.getValue()
            ));
            clearInput();
            refresh();
            lblMessage.setText("Thêm tài sản thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể thêm tài sản!");
        }
    }

    private void addButtonToTable() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDel  = new Button("Xóa");
            private final HBox box = new HBox(5, btnEdit, btnDel);

            {
                btnEdit.setOnAction(e ->
                        editAsset(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e ->
                        deleteAsset(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void editAsset(assets a) {
        if (!requireAdmin()) return;

        TextField tfName = new TextField(a.getName());
        TextField tfType = new TextField(a.getType());
        TextField tfQty  = new TextField(String.valueOf(a.getQuantity()));
        ComboBox<String> cb = new ComboBox<>(cbStatus.getItems());
        cb.setValue(a.getStatus());

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Sửa tài sản");
        d.getDialogPane().setContent(new VBox(5, tfName, tfType, tfQty, cb));
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                Integer q = parseInt(tfQty.getText());
                if (q == null) return;
                a.setName(tfName.getText());
                a.setType(tfType.getText());
                a.setQuantity(q);
                a.setStatus(cb.getValue());
                updateAsset(a);
            }
        });
    }

    private void deleteAsset(assets a) {
        if (!requireAdmin()) return;
        if (!showConfirm("Xác nhận", "Xóa tài sản này?")) return;

        try {
            AssetDao.deleteById(a.getId());
            refresh();
        } catch (Exception e) {
            showError("Lỗi", "Không thể xóa!");
        }
    }

    private void updateAsset(assets a) {
        try {
            AssetDao.update(a);
            refresh();
        } catch (Exception e) {
            showError("Lỗi", "Không thể cập nhật!");
        }
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            showWarning("Sai dữ liệu", "Số lượng phải là số!");
            return null;
        }
    }

    private void clearInput() {
        txtName.clear();
        txtType.clear();
        txtQuantity.clear();
        cbStatus.setValue(null);
    }
}