package pl.edu.agh.io.umniedziala.viewController;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.edu.agh.io.umniedziala.windowsHandlers.WindowsFunctionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChooseApplicationController {
    private AppController appController;
    private Stage stage;
    private ObservableList<App> observableApps;
    private Map<String, String> proccesses;
    final static String DEFAULT_COLOR = "#000000";
    final FileChooser fileChooser = new FileChooser();
    private ManagingApplicationsController managingApplicationsController;
    private Boolean fileChosen;
    private File fileToAdd;

    @FXML
    private TableView appTable;

    @FXML
    private TableColumn<App, Image> iconColumn;

    @FXML
    private TableColumn<App, String> applicationColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button filePicker;

    @FXML
    private Label fileName;

    @FXML
    private Label errorText;

    @FXML
    public void initialize() {
        fileChosen = false;
        managingApplicationsController = new ManagingApplicationsController();
        appTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        applicationColumn.setCellValueFactory(dataValue -> dataValue.getValue().nameProperty());

        iconColumn.setCellValueFactory(data -> new SimpleObjectProperty<Image>(data.getValue().getImage()));
        iconColumn.setCellFactory(param -> {
            final ImageView imageview = new ImageView();
            imageview.setFitHeight(32);
            imageview.setFitWidth(32);

            TableCell<App, Image> cell = new TableCell<App, Image>() {
                @Override
                protected void updateItem(Image item, boolean empty) {
                    if (item != null) {
                        imageview.setImage(item);
                    }
                }
            };
            cell.setGraphic(imageview);
            return cell;
        });

    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void loadData() {
        proccesses = WindowsFunctionHandler.getAllRunningProcesses();
        observableApps = FXCollections.observableArrayList();
        proccesses.forEach((key,value) -> observableApps.add(new App(key, value)));
        appTable.setItems(observableApps);
    }

    @FXML
    public void handleAddButton(ActionEvent event) {
        if (fileChosen){
            if (managingApplicationsController.addNewApplicationByPath(fileToAdd.getAbsolutePath(), DEFAULT_COLOR)) {
                new Alert(Alert.AlertType.INFORMATION, "Dodano", ButtonType.OK).showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "Nie udało się dodać aplikacji", ButtonType.OK).showAndWait();
            }
        }
        ObservableList<App> appsToAdd = appTable.getSelectionModel().getSelectedItems();
        for (App app : appsToAdd){
            if (managingApplicationsController.addNewApplicationByPath(app.getPath(), app.getColor())) {
                new Alert(Alert.AlertType.INFORMATION, "Dodano", ButtonType.OK).showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "Nie udało się dodać aplikacji", ButtonType.OK).showAndWait();
            }
        }
        if (appsToAdd.isEmpty() && !fileChosen){
            errorText.setText("Choose application to add");
        } else {
            stage.close();
        }
    }

    public void handleFilePicker(ActionEvent event) {
        fileToAdd = fileChooser.showOpenDialog(new Stage());
        if (fileToAdd != null) {
            fileChosen = true;
            fileName.setText(fileToAdd.getName());
        }
    }

    private class App {
        StringProperty name;
        String color;
        String path;
        private Image image;

        public App(String name, String path) {
            this.name = new SimpleStringProperty(name);
            this.color = DEFAULT_COLOR;
            this.path = path;
            setImageByPath(path);
        }

        private void setImageByPath(String path) {
            try {
                String fixedPath = path.replace('\\', '/');
                File file = new File(fixedPath);

                sun.awt.shell.ShellFolder sf = sun.awt.shell.ShellFolder.getShellFolder(file);
                ImageIcon swingImageIcon = new ImageIcon(sf.getIcon(true));
                BufferedImage bi = new BufferedImage(
                        swingImageIcon.getIconWidth(),
                        swingImageIcon.getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.createGraphics();
                swingImageIcon.paintIcon(null, g, 0, 0);
                g.dispose();
                this.image = SwingFXUtils.toFXImage(bi, null);
            } catch (FileNotFoundException e) {
                this.image = new Image("file:krimit.png");
            }
        }

        public  String getPath() { return path; }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }
    }
}
