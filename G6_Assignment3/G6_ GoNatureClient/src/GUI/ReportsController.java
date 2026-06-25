package GUI;

import Client.ClientUI;
import Client.OrderClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

/**
 * Controller class for the {@code Reports.fxml} view.
 * <p>
 * This class is responsible for fetching, generating, rendering, and exporting analytic reporting 
 * structures within the GoNature application. It handles four primary report variants: Visits, 
 * Park Usage, Cancellations, and Visitor Count summaries. The view dynamically adjusts layout elements 
 * (such as multi-series BarCharts, PieCharts, and TableViews) based on the specific authorization role 
 * of the logged-in supervisor (Park Manager vs. Department Manager).
 * </p>
 *
 * @author GoNature Development Team
 * @version 1.0
 */
public class ReportsController {

    /** Button component triggering the monthly park visits and stay-duration breakdown report. */
    @FXML private Button btnVisitsReport;
    
    /** Button component triggering the daily park capacity occupancy utilization report. */
    @FXML private Button btnUsageReport;
    
    /** Button component triggering the cancellation and no-show analysis report. */
    @FXML private Button btnCancellationsReport;
    
    /** Button component triggering the total segmented visitor type headcount report. */
    @FXML private Button btnVisitorCountReport;
    
    /** Status message label tracking the current action, metadata summaries, or operational errors. */
    @FXML private Label lblStatus;
    
    /** Container panel placeholder inside which dynamic charting elements (Pie/Bar) are visually injected. */
    @FXML private StackPane chartPane;
    
    /** TableView template rendering the raw underlying matrix database rows matching the requested report. */
    @FXML private TableView<ArrayList<String>> reportTable;
    
    /** ComboBox element providing target calendar month query bounds selection (January - December). */
    @FXML private ComboBox<String> monthBox;
    
    /** ComboBox element providing target calendar year query bounds selection. */
    @FXML private ComboBox<String> yearBox;
    
    /** Descriptive textual label prefixed before the multi-park dropdown layout selection field. */
    @FXML private Label lblPark;
    
    /** ComboBox displaying selectable national parks, isolated for Department Manager scope overrides. */
    @FXML private ComboBox<String> parkBox;
    
    /** Map linking localized park name strings to their corresponding unique numeric internal database primary IDs. */
    private HashMap<String, Integer> parkIdMap = new HashMap<>();
    
    /** Internal state tracker tracking the currently active running report keyword ("VISITS", "USAGE", etc.). */
    private String currentReport = "";
    
    /** Caches the permission role identifier of the active system session user. */
    private String userRole = "";
    
    /** Stores the specific native operating national park identifier attached to a localized Park Manager profile. */
    private int userParkId = -1;

    /** Constant array indexing the standard English naming conventions sequence matching calendar months. */
    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    /**
     * Bootstraps foundational profile properties and privileges defining how data arrays are filtered.
     * <p>
     * Hides specific reporting features unavailable to standalone Park Managers or enables cross-park 
     * search filtering elements specifically for regional Department Managers.
     * </p>
     *
     * @param role   The security role authorization string of the authenticated user.
     * @param parkId Unique identification number of the park managed by the user (-1 if regional).
     */
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

    /**
     * Populates the departmental combo box filter option tree with active national park models retrieved from the server.
     * Called asynchronously from network connection incoming packet listeners.
     *
     * @param parks Nested string array matrix containing database park records.
     */
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
    
    /**
     * Resolves the target destination identification query scope constraint.
     *
     * @return Target internal numeric park database identifier key (-1 represents a global cross-park search request).
     */
    private int getSelectedParkId() {
        if ("DEPARTMENT_MANAGER".equals(userRole)) {
            String selected = parkBox.getValue();
            if (selected == null || "All Parks".equals(selected)) return -1;
            return parkIdMap.getOrDefault(selected, -1);
        }
        return userParkId;
    }

    /**
     * Initializes structural combo box selections automatically upon establishing the FXML window node stage.
     * Sets historical choices targeting a 3-year trailing window and targets the previous trailing calendar month default.
     */
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

    /**
     * Maps the combo box selection index directly to a standard numerical month sequence number.
     *
     * @return Integer sequence identifying the targeted calendar month (1 through 12).
     */
    private int getSelectedMonth() {
        return monthBox.getSelectionModel().getSelectedIndex() + 1;
    }

    /**
     * Pulls the selected numeric string character series translating the target report calendar year boundaries.
     *
     * @return Numerical calendar year value.
     */
    private int getSelectedYear() {
        String y = yearBox.getValue();
        return y != null ? Integer.parseInt(y) : LocalDate.now().getYear();
    }

    /**
     * Validates that the month and year criteria fields are correctly populated prior to compiling server query requests.
     *
     * @return {@code true} if both inputs hold valid non-null criteria selections; {@code false} otherwise.
     */
    private boolean validateMonthYear() {
        if (monthBox.getValue() == null || yearBox.getValue() == null) {
            lblStatus.setText("Please select a month and year first.");
            return false;
        }
        return true;
    }

    /**
     * Dispatches a structured transaction command upstream toward the server channel pulling entry-exit traffic records.
     */
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

    /**
     * Dispatches a structured transaction command upstream toward the server channel pulling occupancy-to-capacity metrics.
     */
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

    /**
     * Dispatches a structured transaction command upstream toward the server channel pulling data regarding cancelled bookings.
     */
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

    /**
     * Dispatches a structured transaction command upstream toward the server channel pulling visitor type breakdowns.
     */
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

    /**
     * Intercepts incoming matrix data buffers from the server framework and routes them 
     * to the appropriate chart-building engine based on the current context tracker.
     * Called asynchronously from network client threads.
     *
     * @param data Multi-dimensional list collection containing records returned from database queries.
     */
    public void setReportData(ArrayList<ArrayList<String>> data) {
        switch (currentReport) {
            case "VISITS":         buildVisitsReport(data != null ? data : new ArrayList<>()); break;
            case "USAGE":          buildUsageReport(data != null ? data : new ArrayList<>()); break;
            case "CANCELLATIONS":  buildCancellationsReport(data != null ? data : new ArrayList<>()); break;
            case "VISITOR_COUNT":  buildVisitorCountReport(data != null ? data : new ArrayList<>()); break;
        }
    }

    /**
     * Compiles data records to construct a multi-series BarChart tracking visitor volume entry trends by calendar day.
     * Segments bars within individual categories by visitor group classification models.
     *
     * @param data Multi-dimensional tracking payload mapping the raw operational visit records.
     */
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

    /**
     * Assembles a double-bar comparative chart rendering total daily park attendance numbers alongside remaining open quota limits.
     *
     * @param data Multi-dimensional analytics tracking records array.
     */
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

    /**
     * Builds a comprehensive PieChart mapping proportional cancellation distributions (e.g., proactive cancellation vs. automated no-show).
     * Calculates mathematical statistics including mean average daily incident rates.
     *
     * @param data Multi-dimensional dataset tracking unfulfilled booking profiles.
     */
    private void buildCancellationsReport(ArrayList<ArrayList<String>> data) {
        String monthLabel = monthBox.getValue() + " " + yearBox.getValue();

        if (data.isEmpty()) {
            lblStatus.setText("No cancellations found for " + monthLabel + ".");
            buildTable(data, new String[]{"Park","Date","Status","Count","Total Visitors"}, new int[]{0,1,2,3,4});
            return;
        }

        Map<String, Integer> countByStatus = new LinkedHashMap<>();
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

    /**
     * Builds an illustrative visual distribution PieChart displaying cumulative headcount volumes divided by visitor type.
     * Segments tracking counts between Group Guides, Subscribers, and Individual public travelers.
     *
     * @param data Multi-dimensional dataset profiling visitor classification fields.
     */
    private void buildVisitorCountReport(ArrayList<ArrayList<String>> data) {
        String monthLabel = monthBox.getValue() + " " + yearBox.getValue();

        if (data.isEmpty()) {
            lblStatus.setText("No visitor count data found for " + monthLabel + ".");
            buildTable(data, new String[]{"Park","Visitor Type","Total Visitors"}, new int[]{0,1,2});
            return;
        }

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

    /**
     * Styles BarChart nodes with custom colors and dark theme styles using a dynamic raw CSS injection pipeline.
     * Modifies graph canvas grids, labels, text spacing fill properties, and operational legends on the JavaFX application thread.
     *
     * @param chart The {@link BarChart} node targeting visual transformation properties.
     */
    private void styleBarChart(BarChart<String, Number> chart) {
        String[] barColors = {"#e53935", "#fb8c00", "#43a047", "#1e88e5", "#8e24aa"};

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

    /**
     * Constructs table columns dynamically to display underlying report rows.
     * Automatically formats raw database floats (truncating unnecessary double precision strings ending in {@code .0}).
     *
     * @param data       Multi-dimensional database matrix records collection.
     * @param headers    Array sequence listing localized programmatic grid view column header names.
     * @param colIndexes Integer mapping indices specifying how values map to internal row index lookups.
     */
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

    /**
     * Flushes active layout properties, resetting charts, rows, and grid parameters 
     * before initiating a brand new network data transaction query request.
     */
    private void clearAll() {
        chartPane.getChildren().clear();
        reportTable.getColumns().clear();
        reportTable.getItems().clear();
        lblStatus.setText("Loading...");
    }

    /**
     * Aborts active analytical workflows and re-allocates window scene nodes back 
     * toward the user role's primary structural dashboard menu panel view.
     */
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