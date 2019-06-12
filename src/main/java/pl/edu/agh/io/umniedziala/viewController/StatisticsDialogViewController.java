/**
 * Created by kuba on 2019-06-12
 */

package pl.edu.agh.io.umniedziala.viewController;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.LocalDate;

public class StatisticsDialogViewController {
    private AppController appController;
    private Stage stage;

    public Button showButton;

    @FXML
    private Label alertLabel;

    @FXML
    private DatePicker startTime;

    @FXML
    private DatePicker endTime;

    @FXML
    public void initialize(){
        startTime.setValue(LocalDate.now().minusDays(7));
        endTime.setValue(LocalDate.now());
    }


    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleShowButton(ActionEvent event) {
        if(endTime.getValue().compareTo(startTime.getValue())>0){
            stage.close();
            appController.showStatisticsView(startTime.getValue().toString(), endTime.getValue().toString());
        } else {
            alertLabel.setText("Invalid date range!");
        }
    }

}