package GUI;

import Client.ClientUI;
import Client.OrderClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.HashMap;

import java.time.LocalDate;
import java.util.*;

public class ReportsController {

    @FXML private Button btnVisitsReport;
    @FXML private Button btnUsageReport;
    @FXML private Button btnCancellationsReport;
    @FXML private Button btnVisitorCountReport;
    @FXML private Label lblStatus;
    @FXML private StackPane chartPane;
    @FXML private TableView<ArrayList<String>> reportTable;
    @FXML private ComboBox<String> monthBox;
    @FXML private ComboBox<String> yearBox;
    @FXML private Label lblPark;
    @FXML private ComboBox<String> parkBox;
    private HashMap<String, Integer> parkIdMap = new HashMap<>();
    private String currentReport = "";
    private String userRole = "";
    private int userParkId = -1;

    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public void initData(String role, int parkId) {
        this.userRole = role;
        this.userParkId = parkId;
        if ("PARK_MANAGER".equals(role)) {
            btnCancellationsReport.setVisible(false);
            btnCancellationsReport.setManaged(false);
            btnVisitorCountReport.setVisible(false);
            btnVisitorCountReport.setManaged(false);
        }
        if ("DEPARTMENT_MANAGER".equals(role)) {
            btnUsageReport.setVisible(false);
            btnUsageReport.setManaged(false);
            btnVisitorCountReport.setVisible(false);
            btnVisitorCountReport.setManaged(false);
            lblPark.setVisible(true);
            lblPark.setManaged(true);
            parkBox.setVisible(true);
            parkBox.setManaged(true);
            OrderClient.reportsController = this;
            ClientUI.client.requestParks();
        }
    }

    public void setParks(ArrayList<ArrayList<String>> parks) {
        parkIdMap.clear();
        parkBox.getItems().clear();
        parkBox.getItems().add("All Parks");
        for (ArrayList<String> row : parks) {
            String parkName = row.get(1);
            int pid = Integer.parseInt(row.get(0));
            parkIdMap.put(parkName, pid);
            parkBox.getItems().add(parkName);
        }
        parkBox.getSelectionModel().selectFirst();
    }
    
    private int getSelectedParkId() {
        if ("DEPARTMENT_MANAGER".equals(userRole)) {
            String selected = parkBox.getValue();
            if (selected == null || "All Parks".equals(selected)) return -1;
            return parkIdMap.getOrDefault(selected, -1);
        }
        return userParkId;
    }
    @FXML
    public void initialize() {
        monthBox.getItems().addAll(MONTHS);
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y >= currentYear - 2; y--) {
            yearBox.getItems().add(String.valueOf(y));
        }
        int prevMonth = LocalDate.now().getMonthValue() - 1;
        if (prevMonth == 0) prevMonth = 12;
        monthBox.getSelectionModel().select(prevMonth - 1);
        yearBox.getSelectionModel().selectFirst();
    }

    private int getSelectedMonth() {
        return monthBox.getSelectionModel().getSelectedIndex() + 1;
    }

    private int getSelectedYear() {
        String y = yearBox.getValue();
        return y != null ? Integer.parseInt(y) : LocalDate.now().getYear();
    }

    private boolean validateMonthYear() {
        if (monthBox.getValue() == null || yearBox.getValue() == null) {
            lblStatus.setText("Please select a month and year first.");
            return false;
        }
        return true;
    }

    @FXML
    private void onVisitsReport() {
        if (!validateMonthYear()) return;
        currentReport = "VISITS";
        lblStatus.setText("Loading visits report...");
        clearAll();
        OrderClient.reportsController = this;
        int month = getSelectedMonth();
        int year  = getSelectedYear();
        int selectedPark = getSelectedParkId();
        if (selectedPark == -1) {
            ClientUI.client.getVisitsReportAll(month, year);
        } else {
            ClientUI.client.getVisitsReport(selectedPark, month, year);
        }
    }

    @FXML
    private void onUsageReport() {
        if (!validateMonthYear()) return;
        currentReport = "USAGE";
        lblStatus.setText("Loading usage report...");
        clearAll();
        OrderClient.reportsController = this;
        int selectedPark = getSelectedParkId();
        if (selectedPark == -1) {
            ClientUI.client.getUsageReport(0, getSelectedMonth(), getSelectedYear());
        } else {
            ClientUI.client.getUsageReport(selectedPark, getSelectedMonth(), getSelectedYear());
        }
    }

    @FXML
    private void onCancellationsReport() {
        if (!validateMonthYear()) return;
        currentReport = "CANCELLATIONS";
        lblStatus.setText("Loading cancellations report...");
        clearAll();
        OrderClient.reportsController = this;
        int selectedPark = getSelectedParkId();
        if (selectedPark == -1) {
            ClientUI.client.getCancellationsReport(getSelectedMonth(), getSelectedYear());
        } else {
            ClientUI.client.getCancellationsReportByPark(selectedPark, getSelectedMonth(), getSelectedYear());
        }
    }

    @FXML
    private void onVisitorCountReport() {
        if (!validateMonthYear()) return;
        currentReport = "VISITOR_COUNT";
        lblStatus.setText("Loading visitor count report...");
        clearAll();
        OrderClient.reportsController = this;
        int month = getSelectedMonth();
        int year  = getSelectedYear();
        int selectedPark = getSelectedParkId();
        if (selectedPark == -1) {
            ClientUI.client.getVisitorCountReportAll(month, year);
        } else {
            ClientUI.client.getVisitorCountReport(selectedPark, month, year);
        }
    }

    public void setReportData(ArrayList<ArrayList<String>> data) {
        switch (currentReport) {
            case "VISITS":         buildVisitsReport(data != null ? data : new ArrayList<>()); break;
            case "USAGE":          buildUsageReport(data != null ? data : new ArrayList<>()); break;
            case "CANCELLATIONS":  buildCancellationsReport(data != null ? data : new ArrayList<>()); break;
            case "VISITOR_COUNT":  buildVisitorCountReport(data != null ? data : new ArrayList<>()); break;
        }
    }

    private void buildVisitsReport(ArrayList<ArrayList<String>> data) {
        String monthLabel = monthBox.getValue() + " " + yearBox.getValue();

        if (data.isEmpty()) {
            lblStatus.setText("No visit data found for " + monthLabel + ".");
            buildTable(data, new String[]{"Park","Entry Time","Exit Time","Visitors","Type","Stay"}, new int[]{0,1,2,3,4,5});
            return;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day of Month");
        yAxis.setLabel("Total Visitors");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#a8d5a2"));
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#a8d5a2"));

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Visits by Day — " + monthLabel);
        barChart.setStyle("-fx-background-color: transparent; -fx-text-fill: #a8d5a2;");
        barChart.setBarGap(2);
        barChart.setCategoryGap(12);
        barChart.setAnimated(true);

        Map<String, Map<String, Integer>> dayTypeMap = new LinkedHashMap<>();
        Set<String> types = new LinkedHashSet<>();
        for (ArrayList<String> row : data) {
            String day = "Day " + row.get(6);
            String type = row.get(4);
            int visitors = Integer.parseInt(row.get(3));
            types.add(type);
            dayTypeMap.computeIfAbsent(day, k -> new LinkedHashMap<>())
                      .merge(type, visitors, Integer::sum);
        }

        for (String type : types) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(type);
            for (Map.Entry<String, Map<String, Integer>> dayEntry : dayTypeMap.entrySet()) {
                int val = dayEntry.getValue().getOrDefault(type, 0);
                series.getData().add(new XYChart.Data<>(dayEntry.getKey(), val));
            }
            barChart.getData().add(series);
        }

        styleBarChart(barChart);
        chartPane.getChildren().setAll(barChart);
        buildTable(data,
            new String[]{"Park","Entry Time","Exit Time","Visitors","Type","Stay"},
            new int[]{0,1,2,3,4,5});
        lblStatus.setText("Visits report — " + monthLabel + " — " + data.size() + " records.");
    }

    private void buildUsageReport(ArrayList<ArrayList<String>> data) {
        String monthLabel = monthBox.getValue() + " " + yearBox.getValue();

        if (data.isEmpty()) {
            lblStatus.setText("No usage data found for " + monthLabel + ".");
            buildTable(data, new String[]{"Day","Visitors","Max Capacity","Available Quota"}, new int[]{0,1,2,3});
            return;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day of Month");
        yAxis.setLabel("Count");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#a8d5a2"));
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#a8d5a2"));

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Daily Usage — " + monthLabel);
        barChart.setStyle("-fx-background-color: transparent;");
        barChart.setBarGap(2);
        barChart.setCategoryGap(12);
        barChart.setAnimated(true);

        XYChart.Series<String, Number> visitorSeries = new XYChart.Series<>();
        visitorSeries.setName("Visitors");
        XYChart.Series<String, Number> availableSeries = new XYChart.Series<>();
        availableSeries.setName("Available Quota");

        for (ArrayList<String> row : data) {
            String day    = row.get(0);
            int visitors  = Integer.parseInt(row.get(1));
            int available = Integer.parseInt(row.get(3));
            visitorSeries.getData().add(new XYChart.Data<>(day, visitors));
            availableSeries.getData().add(new XYChart.Data<>(day, available));
        }

        barChart.getData().addAll(visitorSeries, availableSeries);
        styleBarChart(barChart);
        chartPane.getChildren().setAll(barChart);
        buildTable(data,
            new String[]{"Day","Total Visitors","Max Capacity","Available Quota"},
            new int[]{0,1,2,3});
        lblStatus.setText("Usage report — " + monthLabel + " — " + data.size() + " days.");
    }

    private void buildCancellationsReport(ArrayList<ArrayList<String>> data) {
        String monthLabel = monthBox.getValue() + " " + yearBox.getValue();

        if (data.isEmpty()) {
            lblStatus.setText("No cancellations found for " + monthLabel + ".");
            buildTable(data, new String[]{"Park","Date","Status","Count","Total Visitors"}, new int[]{0,1,2,3,4});
            return;
        }

        // Count by status for pie chart
        Map<String, Integer> countByStatus = new LinkedHashMap<>();
        // Count by date for average calculation
        Map<String, Integer> countByDate = new LinkedHashMap<>();
        int totalCancelled = 0;
        int totalNoShow = 0;

        for (ArrayList<String> row : data) {
            String status = row.get(2);
            int count = Integer.parseInt(row.get(3));
            String date = row.get(1);

            countByStatus.merge(status, count, Integer::sum);
            countByDate.merge(date, count, Integer::sum);

            if ("CANCELLED".equals(status)) totalCancelled += count;
            else if ("NO_SHOW".equals(status)) totalNoShow += count;
        }

        int totalDays = countByDate.size();
        int totalAll = totalCancelled + totalNoShow;
        double avgPerDay = totalDays > 0 ? (double) totalAll / totalDays : 0;

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Cancellations — " + monthLabel);
        pieChart.setStyle("-fx-background-color: transparent;");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setAnimated(true);

        String[] pieColors = {"#e53935", "#fb8c00", "#8e24aa", "#1e88e5"};
        for (Map.Entry<String, Integer> entry : countByStatus.entrySet()) {
            pieChart.getData().add(new PieChart.Data(
                entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        javafx.application.Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data d : pieChart.getData()) {
                d.getNode().setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
                i++;
            }
            pieChart.lookupAll(".chart-legend-item").forEach(n ->
                n.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"));
            pieChart.lookupAll(".chart-title").forEach(n ->
                n.setStyle("-fx-text-fill: #a8d5a2; -fx-font-size: 14px; -fx-font-weight: bold;"));
            pieChart.lookupAll(".chart-legend").forEach(n ->
                n.setStyle("-fx-background-color: transparent;"));
        });

        chartPane.getChildren().setAll(pieChart);
        buildTable(data,
            new String[]{"Park","Date","Status","Count","Total Visitors"},
            new int[]{0,1,2,3,4});

        lblStatus.setText(String.format(
            "Cancellations — %s — %d cancelled | %d no-show | %d active days | Avg %.1f cancellations/day",
            monthLabel, totalCancelled, totalNoShow, totalDays, avgPerDay));
    }

    private void buildVisitorCountReport(ArrayList<ArrayList<String>> data) {
        String monthLabel = monthBox.getValue() + " " + yearBox.getValue();

        if (data.isEmpty()) {
            lblStatus.setText("No visitor count data found for " + monthLabel + ".");
            buildTable(data, new String[]{"Park","Visitor Type","Total Visitors"}, new int[]{0,1,2});
            return;
        }

        // Pie chart showing breakdown by visitor type
        Map<String, Integer> countByType = new LinkedHashMap<>();
        for (ArrayList<String> row : data) {
            countByType.merge(row.get(1), Integer.parseInt(row.get(2)), Integer::sum);
        }

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Visitor Count by Type — " + monthLabel);
        pieChart.setStyle("-fx-background-color: transparent;");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setAnimated(true);

        String[] pieColors = {"#4caf50","#2196f3","#ff9800","#e91e63"};
        int total = 0;
        for (Map.Entry<String, Integer> entry : countByType.entrySet()) {
            pieChart.getData().add(new PieChart.Data(
                entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            total += entry.getValue();
        }

        final int finalTotal = total;
        javafx.application.Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data d : pieChart.getData()) {
                d.getNode().setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
                i++;
            }
            pieChart.lookupAll(".chart-legend-item").forEach(n ->
                n.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"));
            pieChart.lookupAll(".chart-title").forEach(n ->
                n.setStyle("-fx-text-fill: #a8d5a2; -fx-font-size: 14px; -fx-font-weight: bold;"));
            pieChart.lookupAll(".chart-legend").forEach(n ->
                n.setStyle("-fx-background-color: transparent;"));
        });

        chartPane.getChildren().setAll(pieChart);
        buildTable(data,
            new String[]{"Park","Visitor Type","Total Visitors"},
            new int[]{0,1,2});
        lblStatus.setText("Visitor Count — " + monthLabel + " — " + total + " total visitors.");
    }

    private void styleBarChart(BarChart<String, Number> chart) {
        String[] barColors = {"#e53935", "#fb8c00", "#43a047", "#1e88e5", "#8e24aa"};

        // Inject CSS directly into the chart so colors apply before render
        StringBuilder css = new StringBuilder();
        for (int i = 0; i < barColors.length; i++) {
            css.append(".default-color").append(i).append(".chart-bar { -fx-bar-fill: ")
               .append(barColors[i]).append("; }\n");
        }
        chart.getStylesheets().clear();
        String cssData = "data:text/css," + css.toString().replace("\n", "%0A").replace(" ", "%20").replace("{", "%7B").replace("}", "%7D").replace(":", "%3A").replace(";", "%3B");
        chart.getStylesheets().add(cssData);

        javafx.application.Platform.runLater(() -> {
            chart.lookupAll(".chart-plot-background").forEach(n ->
                n.setStyle("-fx-background-color: #1e3a1e;"));
            chart.lookupAll(".chart-legend").forEach(n ->
                n.setStyle("-fx-background-color: transparent;"));
            chart.lookupAll(".chart-title").forEach(n ->
                n.setStyle("-fx-text-fill: #a8d5a2; -fx-font-size: 14px; -fx-font-weight: bold;"));
            chart.lookupAll(".axis-label").forEach(n ->
                n.setStyle("-fx-text-fill: #a8d5a2; -fx-font-size: 12px;"));
            chart.lookupAll(".chart-legend-item").forEach(n -> {
                n.setStyle("-fx-text-fill: white;");
                if (n instanceof javafx.scene.layout.HBox) {
                    ((javafx.scene.layout.HBox) n).getChildren().forEach(child -> {
                        if (child instanceof javafx.scene.control.Label) {
                            ((javafx.scene.control.Label) child).setStyle(
                                "-fx-text-fill: white; -fx-font-size: 12px;");
                        }
                    });
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void buildTable(ArrayList<ArrayList<String>> data, String[] headers, int[] colIndexes) {
        reportTable.getColumns().clear();
        reportTable.getItems().clear();
        reportTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        for (int i = 0; i < headers.length; i++) {
            final int colIndex = colIndexes[i];
            TableColumn<ArrayList<String>, String> col = new TableColumn<>(headers[i]);
            col.setMinWidth(80);
            col.setCellValueFactory(cellData -> {
                ArrayList<String> row = cellData.getValue();
                String value = (colIndex < row.size() && row.get(colIndex) != null)
                               ? row.get(colIndex) : "";
                if (value.endsWith(".0")) value = value.substring(0, value.length() - 2);
                return new javafx.beans.property.SimpleStringProperty(value);
            });
            col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); }
                    else {
                        setText(item);
                        setStyle("-fx-text-fill: #1a2e1a; -fx-font-size: 12px; -fx-padding: 4 8;");
                    }
                }
            });
            reportTable.getColumns().add(col);
        }

        reportTable.getItems().addAll(data);
        javafx.application.Platform.runLater(() -> {
            reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            reportTable.refresh();
        });
    }

    private void clearAll() {
        chartPane.getChildren().clear();
        reportTable.getColumns().clear();
        reportTable.getItems().clear();
        lblStatus.setText("Loading...");
    }

    @FXML
    private void onBack() {
        try {
            OrderClient.reportsController = null;
            String fxml = "PARK_MANAGER".equals(userRole)
                ? "/GUI/ParkManagerDashboard.fxml"
                : "/GUI/DeptManagerDashboard.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) lblStatus.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}