/**
 * Created by kuba on 06/04/2019
 */

package pl.edu.agh.io.umniedziala.viewController;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Date;

public class AppController {
    private Stage primaryStage;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    public void initRootLayout() throws IOException {
        this.primaryStage.setTitle("WorkMonitor");

        // load layout from FXML file
        FXMLLoader loader = new FXMLLoader();
        Parent rootLayout = loader.load(getClass().getResourceAsStream("/views/MainView.fxml"));


        MainViewController controller = loader.getController();
        controller.setAppController(this);

        controller.startTimechartUpdates();

        // nie aktualizujemy wykresu kiedy aplikacja jest zminimalizowana
        // i aktualizujemy regularnie (oraz natychmiast) kiedy wróci na ekran
        this.primaryStage.iconifiedProperty().addListener((ov, t, t1) -> {
            if(t1.booleanValue()){
                // jest zminimalizowana
                controller.stopTimechartUpdates();
            } else {
                // jest na erkanie
                controller.startTimechartUpdates();
            }
        });

        // add layout to a scene and show them all
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(450);
        primaryStage.show();

    }

    private void closeWindowEvent(WindowEvent event) {
        System.out.println("Window close request ...");
        System.exit(0);
    }

    private Boolean appWindowLoaded = false;
    public void showAddApplicationWindow() {
        if (!appWindowLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader();

                Parent page = loader.load(getClass().getResourceAsStream("/views/ChooseApplicationView.fxml"));

                Stage appStage = new Stage();
                appStage.setTitle("Choose application");
                appStage.setResizable(false);

                Scene scene = new Scene(page);
                appStage.setScene(scene);

                ChooseApplicationController controller = loader.getController();
                controller.setAppController(this);
                controller.setStage(appStage);
                controller.loadData();
                appStage.show();
                appStage.setAlwaysOnTop(true);

                appWindowLoaded = true;
                appStage.setOnCloseRequest((WindowEvent event) -> { appWindowLoaded = false; });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean reportWindowLoaded = false;
    public void showReportGenerationWindow() {
        if (!reportWindowLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader();

                Parent page = loader.load(getClass().getResourceAsStream("/views/ReportGenerationView.fxml"));

                Stage reportStage = new Stage();
                reportStage.setTitle("Report Generation");
                reportStage.setResizable(false);

                Scene scene = new Scene(page);
                reportStage.setScene(scene);

                ReportGenerationViewController controller = loader.getController();
                controller.setAppController(this);
                controller.setStage(reportStage);
                reportStage.show();
                reportStage.setAlwaysOnTop(true);

                reportWindowLoaded = true;
                reportStage.setOnCloseRequest((WindowEvent event) -> { reportWindowLoaded = false; });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showSettingsWindow() {
            try {
                FXMLLoader loader = new FXMLLoader();

                Parent page = loader.load(getClass().getResourceAsStream("/views/SettingsView.fxml"));

                Stage settingsStage = new Stage();
                settingsStage.setTitle("Settings");
                settingsStage.setResizable(false);

                Scene scene = new Scene(page);
                settingsStage.setScene(scene);

                SettingsViewController controller = loader.getController();
                controller.setAppController(this);
                controller.setStage(settingsStage);
                controller.loadData();
                settingsStage.show();
                settingsStage.setAlwaysOnTop(true);

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void showCustomEventView(Date date) {
            try {
                // Load the fxml file and create a new stage for the dialog
                FXMLLoader loader = new FXMLLoader();

                Parent page = loader.load(getClass().getResourceAsStream("/views/CustomEventView.fxml"));

                // Create the dialog Stage.
                Stage eventStage = new Stage();
                eventStage.setTitle("Custom event");
                eventStage.setResizable(false);
                Scene scene = new Scene(page);
                eventStage.setScene(scene);

                CustomEventController controller = loader.getController();
                controller.setAppController(this);
                controller.setStage(eventStage);
                controller.setDate(date);
                eventStage.showAndWait();
                eventStage.setAlwaysOnTop(true);


            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private Boolean statisticsWindowLoaded = false;
    public void showStatisticsView(String from, String to) {
        if (!statisticsWindowLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader();

                Parent page = loader.load(getClass().getResourceAsStream("/views/StatisticsView.fxml"));

                Stage statisticsStage = new Stage();
                statisticsStage.setTitle("Statistics");
                statisticsStage.setMinHeight(250);
                statisticsStage.setMinWidth(300);

                Scene scene = new Scene(page);
                statisticsStage.setScene(scene);

                StatisticsViewController controller = loader.getController();
                controller.setAppController(this);
                controller.setStage(statisticsStage);
                controller.loadData(from, to);
                statisticsStage.show();
                statisticsStage.setAlwaysOnTop(true);

                statisticsWindowLoaded = true;
                statisticsStage.setOnCloseRequest((WindowEvent event) -> { statisticsWindowLoaded = false;
                statDialogWindowLoaded = false; });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Boolean statDialogWindowLoaded = false;
    public void showStatisticsDialogView() {
        if (!statDialogWindowLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader();

                Parent page = loader.load(getClass().getResourceAsStream("/views/StatisitcsDialogView.fxml"));

                Stage statisticsStage = new Stage();
                statisticsStage.setTitle("Statistics");

                Scene scene = new Scene(page);
                statisticsStage.setScene(scene);

                StatisticsDialogViewController controller = loader.getController();
                controller.setAppController(this);
                controller.setStage(statisticsStage);
                statisticsStage.show();
                statisticsStage.setAlwaysOnTop(true);

                statDialogWindowLoaded = true;
                statisticsStage.setOnCloseRequest((WindowEvent event) -> { statDialogWindowLoaded = false; });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}