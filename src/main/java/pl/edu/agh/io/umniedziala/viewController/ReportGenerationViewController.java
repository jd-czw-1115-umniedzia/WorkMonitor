/**
 * Created by kuba on 2019-05-08
 */

package pl.edu.agh.io.umniedziala.viewController;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pl.edu.agh.io.umniedziala.ReportsGenerator.BasicReport;

import java.io.IOException;
import java.time.LocalDate;


public class ReportGenerationViewController {

    private AppController appController;
    private Stage stage;


    @FXML
    private DatePicker fromDate;

    @FXML
    private DatePicker toDate;

    @FXML
    private Label errorText;

    @FXML
    private Button generate;

    @FXML
    private CheckBox generateReportApp;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        toDate.setValue(LocalDate.now());
        fromDate.setValue(LocalDate.now().minusDays(7));
    }

    @FXML
    public void generateHandler(ActionEvent event) {
        LocalDate to = toDate.getValue();
        LocalDate from = fromDate.getValue();
        if (from.compareTo(to) <= 0) {
            try {
                BasicReport basicReport = new BasicReport(from, to);
                if (generateReportApp.isSelected())
                    basicReport.createReportWithApps();
                else
                    basicReport.createReportWithoutApps();
                stage.close();
            } catch (IOException e) {
                errorText.setText("Report not generated");
            }
        } else {
            errorText.setText("Invalid date range!");
        }
    }
}