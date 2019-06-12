package pl.edu.agh.io.umniedziala.viewController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pl.edu.agh.io.umniedziala.model.CustomEventEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomEventController {

    private AppController appController;
    private Stage stage;

    Boolean nameInserted;
    Date date;

    @FXML
    private Label alert;

    @FXML
    private TextField nameInput;

    @FXML
    private TextArea descriptionInput;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Button saveButton;

    @FXML
    private TextField startTime;

    @FXML
    private TextField endTime;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize(){
        nameInserted=false;
        startTime.setText("08:00:00");
        endTime.setText("16:00:00");
    }

    public void setDate(Date date){
        this.date = date;
    }

    @FXML
    public void handleSaveButton(ActionEvent event) {
        nameInserted = !nameInput.getText().isEmpty();
        if (nameInserted) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String start = startTime.getText();
            String end = endTime.getText();
            String dateOfEvent = formatter.format(date);
            start = dateOfEvent+" "+start;
            end = dateOfEvent+" "+end;
            Color color = colorPicker.getValue();
            String resultColor = String.format("#%02X%02X%02X",
                    (int)(color.getRed()*255),
                    (int)(color.getGreen()*255),
                    (int)(color.getBlue()*255));
            CustomEventEntity.create(start, end, nameInput.getText(), descriptionInput.getText(), resultColor);
            stage.close();
        } else {
            alert.setText("Choose start time, end time and name!");
        }
    }
}
