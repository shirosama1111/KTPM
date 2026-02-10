package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.TemporaryRecordDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.TemporaryRecord;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class StatisticsController extends BaseController {

    @FXML private Label lblTotalCitizens;
    @FXML private Label lblMale;
    @FXML private Label lblFemale;

    // Biểu đồ độ tuổi
    @FXML private BarChart<String, Number> chartAge;
    @FXML private CategoryAxis xAxisAge;
    @FXML private NumberAxis yAxisAge;

    // Biểu đồ giới tính
    @FXML private PieChart chartGender;

    // Tạm vắng/trú
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label lblTamVang;
    @FXML private Label lblTamTru;
    @FXML private TableView<TemporaryRecord> tableTempRecords;
    @FXML private TableColumn<TemporaryRecord, String> colCitizenName;
    @FXML private TableColumn<TemporaryRecord, String> colType;
    @FXML private TableColumn<TemporaryRecord, String> colStartDate;
    @FXML private TableColumn<TemporaryRecord, String> colEndDate;

    @FXML
    public void initialize() {
        // Set date range mặc định (30 ngày gần nhất)
        dpEndDate.setValue(LocalDate.now());
        dpStartDate.setValue(LocalDate.now().minusDays(30));

        setupTempRecordsTable();
        loadStatistics();
    }

    private void setupTempRecordsTable() {
        colCitizenName.setCellValueFactory(d -> {
            try {
                Citizen c = CitizenDao.findById(d.getValue().getCitizenId());
                return new javafx.beans.property.SimpleStringProperty(
                        c != null ? c.getFullName() : "Unknown"
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        colType.setCellValueFactory(d -> {
            String type = d.getValue().getType();
            String display = type.equals("TAM_VANG") ? "Tạm vắng" : "Tạm trú";
            return new javafx.beans.property.SimpleStringProperty(display);
        });

        colStartDate.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getStartDate() != null ?
                                d.getValue().getStartDate().toString() : ""
                ));

        colEndDate.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getEndDate() != null ?
                                d.getValue().getEndDate().toString() : "Không thời hạn"
                ));
    }

    @FXML
    private void loadStatistics() {
        try {
            List<Citizen> allCitizens = CitizenDao.findAll();

            // ===== THỐNG KÊ TỔNG SỐ =====
            lblTotalCitizens.setText(String.valueOf(allCitizens.size()));

            // ===== THỐNG KÊ GIỚI TÍNH =====
            int male = 0, female = 0, unknown = 0;

            // ===== THỐNG KÊ ĐỘ TUỔI =====
            int mamNon = 0;      // 0-3 tuổi
            int mauGiao = 0;     // 4-5 tuổi
            int cap1 = 0;        // 6-10 tuổi
            int cap2 = 0;        // 11-14 tuổi
            int cap3 = 0;        // 15-17 tuổi
            int laoDong = 0;     // 18-59 tuổi
            int nghiHuu = 0;     // 60+ tuổi

            LocalDate now = LocalDate.now();

            for (Citizen c : allCitizens) {
                // Giới tính (dựa vào CCCD - số thứ 4 từ phải sang)
                String cccd = c.getCccd();
                if (cccd != null && cccd.length() == 12) {
                    char genderDigit = cccd.charAt(3); // Vị trí thứ 4
                    if (genderDigit == '0' || genderDigit == '2' || genderDigit == '4' ||
                            genderDigit == '6' || genderDigit == '8') {
                        male++;
                    } else {
                        female++;
                    }
                } else {
                    unknown++;
                }

                // Độ tuổi
                if (c.getDob() != null) {
                    int age = Period.between(c.getDob(), now).getYears();

                    if (age >= 0 && age <= 3) mamNon++;
                    else if (age >= 4 && age <= 5) mauGiao++;
                    else if (age >= 6 && age <= 10) cap1++;
                    else if (age >= 11 && age <= 14) cap2++;
                    else if (age >= 15 && age <= 17) cap3++;
                    else if (age >= 18 && age <= 59) laoDong++;
                    else if (age >= 60) nghiHuu++;
                }
            }

            // Cập nhật label giới tính
            lblMale.setText(String.valueOf(male));
            lblFemale.setText(String.valueOf(female));

            // ===== VẼ BIỂU ĐỒ ĐỘ TUỔI =====
            XYChart.Series<String, Number> ageSeries = new XYChart.Series<>();
            ageSeries.setName("Số người");

            ageSeries.getData().add(new XYChart.Data<>("Mầm non (0-3)", mamNon));
            ageSeries.getData().add(new XYChart.Data<>("Mẫu giáo (4-5)", mauGiao));
            ageSeries.getData().add(new XYChart.Data<>("Cấp 1 (6-10)", cap1));
            ageSeries.getData().add(new XYChart.Data<>("Cấp 2 (11-14)", cap2));
            ageSeries.getData().add(new XYChart.Data<>("Cấp 3 (15-17)", cap3));
            ageSeries.getData().add(new XYChart.Data<>("Lao động (18-59)", laoDong));
            ageSeries.getData().add(new XYChart.Data<>("Nghỉ hưu (60+)", nghiHuu));

            chartAge.getData().clear();
            chartAge.getData().add(ageSeries);

            // ===== VẼ BIỂU ĐỒ GIỚI TÍNH =====
            chartGender.getData().clear();
            if (male > 0) {
                chartGender.getData().add(new PieChart.Data("Nam (" + male + ")", male));
            }
            if (female > 0) {
                chartGender.getData().add(new PieChart.Data("Nữ (" + female + ")", female));
            }
            if (unknown > 0) {
                chartGender.getData().add(new PieChart.Data("Không rõ (" + unknown + ")", unknown));
            }

            // ===== TẠM VẮNG/TẠM TRÚ =====
            loadTemporaryRecords();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải thống kê: " + e.getMessage());
        }
    }

    @FXML
    private void loadTemporaryRecords() {
        try {
            LocalDate start = dpStartDate.getValue();
            LocalDate end = dpEndDate.getValue();

            if (start == null || end == null) {
                showWarning("Lỗi", "Vui lòng chọn khoảng thời gian!");
                return;
            }

            if (start.isAfter(end)) {
                showWarning("Lỗi", "Ngày bắt đầu phải trước ngày kết thúc!");
                return;
            }

            List<Citizen> allCitizens = CitizenDao.findAll();
            int tamVangCount = 0;
            int tamTruCount = 0;

            tableTempRecords.getItems().clear();

            for (Citizen c : allCitizens) {
                List<TemporaryRecord> records = TemporaryRecordDao.findByCitizen(c.getId());

                for (TemporaryRecord r : records) {
                    // Kiểm tra xem record có nằm trong khoảng thời gian không
                    boolean inRange = false;

                    if (r.getStartDate() != null) {
                        if (r.getEndDate() != null) {
                            // Có thời hạn
                            if (!r.getStartDate().isAfter(end) &&
                                    !r.getEndDate().isBefore(start)) {
                                inRange = true;
                            }
                        } else {
                            // Không thời hạn
                            if (!r.getStartDate().isAfter(end)) {
                                inRange = true;
                            }
                        }
                    }

                    if (inRange) {
                        tableTempRecords.getItems().add(r);

                        if ("TAM_VANG".equals(r.getType())) {
                            tamVangCount++;
                        } else if ("TAM_TRU".equals(r.getType())) {
                            tamTruCount++;
                        }
                    }
                }
            }

            lblTamVang.setText(String.valueOf(tamVangCount));
            lblTamTru.setText(String.valueOf(tamTruCount));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải dữ liệu tạm vắng/trú: " + e.getMessage());
        }
    }

    @FXML
    private void exportReport() {
        try {
            // TODO: Xuất báo cáo ra file Excel hoặc PDF
            showInfo("Thông báo", "Chức năng xuất báo cáo đang phát triển!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể xuất báo cáo!");
        }
    }

    @FXML
    private void backToMenu() throws Exception {
        Main.showMenu();
    }
}