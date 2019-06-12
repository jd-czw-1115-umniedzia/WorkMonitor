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
    private LocalDate from;
    private LocalDate to;
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
    public void initialize(){
        toDate.setValue(LocalDate.now());
        fromDate.setValue(LocalDate.now().minusDays(7));
        to = toDate.getValue();
        from = fromDate.getValue();
    }

    @FXML
    public void toPickedHandler(ActionEvent event){
        to = toDate.getValue();
    }

    @FXML
    public void fromPickedHandler(ActionEvent event){
        from = fromDate.getValue();
    }

    @FXML
    public void generateHandler(ActionEvent event) {
        if (from.compareTo(to) <= 0){
            try {
                BasicReport basicReport = new BasicReport(from,to);
                if (generateReportApp.isSelected()) basicReport.createReportWithApps();
                else basicReport.createReportWithoutApps();
            } catch (IOException e){
                errorText.setText("Report not generated");
            }
            stage.close();
        } else {
            errorText.setText("Invalid date range!");
        }

    }
}