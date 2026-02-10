    package com.example.demo4.controllers;

    import com.example.demo4.Main;
    import com.example.demo4.dao.HouseholdDao;
    import com.example.demo4.models.Citizen;
    import com.example.demo4.models.Household;
    import com.example.demo4.dao.CitizenDao;
    import javafx.beans.property.SimpleIntegerProperty;
    import javafx.beans.property.SimpleStringProperty;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.ChoiceDialog;
    import javafx.scene.control.TableColumn;
    import javafx.scene.control.TableView;
    import javafx.stage.Stage;

    import java.util.List;

    public class HouseholdAdminController extends BaseController {

        @FXML private TableView<Household> table;
        @FXML private TableColumn<Household, String>  colId;
        @FXML private TableColumn<Household, String>  colOwner;
        @FXML private TableColumn<Household, String>  colAddress;
        @FXML private TableColumn<Household, Integer> colMembers;

        @FXML
        public void initialize() {
            colId.setCellValueFactory(data ->
                    new SimpleStringProperty(String.valueOf(data.getValue().getHouseholdId())));

            colOwner.setCellValueFactory(data -> {
                Integer cid = data.getValue().getHeadCitizenId();
                if (cid == null) return new SimpleStringProperty("");

                try {
                    Citizen c = CitizenDao.findById(cid);
                    if (c != null) {
                        return new SimpleStringProperty(
                                c.getCccd() + " - " + c.getFullName()
                        );
                    }
                } catch (Exception ignored) {}

                return new SimpleStringProperty("CID: " + cid);
            });


            colAddress.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAddress()));

            // Chưa có số thành viên trong model/dao → tạm fix cứng
            colMembers.setCellValueFactory(data -> {
                try {
                    int count = CitizenDao.countByHousehold(
                            data.getValue().getHouseholdId()
                    );
                    return new SimpleIntegerProperty(count).asObject();
                } catch (Exception e) {
                    return new SimpleIntegerProperty(0).asObject();
                }
            });

            loadHouseholds();
        }

        private void loadHouseholds() {
            try {
                table.getItems().setAll(HouseholdDao.findAll());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể tải danh sách hộ khẩu!");
            }
        }

        @FXML
        private void backToMenu() throws Exception {
            Main.showMenu();
        }

        @FXML
        private void addHousehold() {
            if (!requireAdmin()) return;

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/demo4/add_household.fxml"));
                Parent root = loader.load();

                AddHouseholdController controller = loader.getController();
                Stage stage = new Stage();
                controller.setStage(stage);
                controller.setOnAddSuccess(this::loadHouseholds); // reload TableView sau khi thêm

                stage.setTitle("Thêm Hộ khẩu");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở form thêm hộ khẩu!\n" + e.getMessage());
            }
        }

        @FXML
        private void manageMembers() {
            Household selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Lỗi", "Chọn một hộ khẩu!");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/demo4/manage_household_members.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));

                ManageHouseholdMembersController c = loader.getController();
                c.setHouseholdId(selected.getHouseholdId());
                c.setStage(stage);

                stage.setTitle("Quản lý thành viên hộ " + selected.getHouseholdId());
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở màn quản lý thành viên!");
            }
        }

        @FXML
        private void manageHomelessCitizens() {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/com/example/demo4/homeless_citizens.fxml"
                        )
                );
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Cư dân chưa có nơi cư trú");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không mở được danh sách cư dân chưa có nơi cư trú");
            }
        }

        @FXML
        private void manageCitizens() {
            Household selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Lỗi", "Hãy chọn một hộ khẩu trước!");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/demo4/citizen.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Quản lý nhân khẩu");

                CitizenController controller = loader.getController();
                controller.setCurrentHouseholdId(selected.getHouseholdId()); // 🟢 load công dân theo household
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở danh sách nhân khẩu!");
            }
        }

        @FXML
        private void editHousehold() {
            if (!requireAdmin()) return;

            Household selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Lỗi", "Hãy chọn một hộ khẩu để sửa!");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/demo4/edit_household.fxml"));
                Parent root = loader.load();

                EditHouseholdController controller = loader.getController();
                controller.setHousehold(selected);
                Stage stage = new Stage();
                controller.setStage(stage);
                controller.setOnEditSuccess(this::loadHouseholds);

                stage.setTitle("Sửa Hộ khẩu");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở form sửa hộ khẩu!\n" + e.getMessage());
            }
        }

        @FXML
        private void deleteHousehold() {
            if (!requireAdmin()) return;

            Household selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Lỗi", "Hãy chọn một hộ khẩu để xóa!");
                return;
            }

            boolean ok = showConfirm(
                    "Xác nhận",
                    "Bạn có chắc chắn muốn xóa hộ " + selected.getHouseholdId() + "?"
            );
            if (!ok) return;

            try {
                // dùng DAO cho sạch
                HouseholdDao.deleteById(selected.getHouseholdId());

                showInfo("Thành công", "Đã xóa hộ khẩu!");
                loadHouseholds();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể xóa hộ khẩu!\n" + e.getMessage());
            }
        }

        @FXML
        private void viewCitizenHistory() {
            Household selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Lỗi", "Hãy chọn một hộ khẩu!");
                return;
            }

            try {
                // Lấy danh sách công dân trong hộ
                List<Citizen> citizens = CitizenDao.findByHouseholdId(selected.getHouseholdId());

                if (citizens.isEmpty()) {
                    showWarning("Không có dữ liệu", "Hộ này chưa có công dân nào!");
                    return;
                }

                // Hiển thị dialog chọn công dân
                ChoiceDialog<Citizen> dialog = new ChoiceDialog<>(citizens.get(0), citizens);
                dialog.setTitle("Chọn công dân");
                dialog.setHeaderText("Chọn công dân để xem lịch sử thay đổi");
                dialog.setContentText("Công dân:");

                // Custom hiển thị
                dialog.getDialogPane().setContentText("Chọn công dân:");

                dialog.showAndWait().ifPresent(citizen -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/example/demo4/citizen_history.fxml")
                        );
                        Stage stage = new Stage();
                        stage.setScene(new Scene(loader.load()));

                        CitizenHistoryController controller = loader.getController();
                        controller.setCitizen(citizen);

                        stage.setTitle("Lịch sử thay đổi - " + citizen.getFullName());
                        stage.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Lỗi", "Không thể mở lịch sử!");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể tải danh sách công dân!");
            }
        }

        @FXML
        private void splitHousehold() {
            if (!requireAdmin()) return;

            Household selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Lỗi", "Hãy chọn hộ khẩu để tách!");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/demo4/split_household.fxml")
                );
                Parent root = loader.load();

                SplitHouseholdController controller = loader.getController();
                Stage stage = new Stage();
                controller.setStage(stage);
                controller.setOriginalHousehold(selected);
                controller.setOnSplitSuccess(this::loadHouseholds);

                stage.setTitle("Tách hộ khẩu");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở form tách hộ!");
            }
        }

        @FXML
        private void openTemporaryRecords() {
            if (!requireAdmin()) return;

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/demo4/temporary_records.fxml")
                );
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Quản lý tạm vắng/tạm trú");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi", "Không thể mở quản lý tạm vắng/trú!");
            }
        }
    }