/**
 * Created by kuba on 2019-06-12
 */

package pl.edu.agh.io.umniedziala.viewController;


import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import pl.edu.agh.io.umniedziala.model.ApplicationEntity;
import pl.edu.agh.io.umniedziala.model.ReportEntryEntity;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static pl.edu.agh.io.umniedziala.ReportsGenerator.BasicReport.parseResultSet;

public class StatisticsViewController {
    private AppController appController;
    private Stage stage;


    @FXML
    LineChart chart;

    @FXML
    CategoryAxis monthsAxis;

    @FXML
    NumberAxis usageAxis;


    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void loadData(String from, String to){
        List<ReportEntryEntity> entities = parseResultSet(ReportEntryEntity.getReportEntries(from,to));
        Map<Date,List<ReportEntryEntity>> entitiesGroupedByDate =
                entities.stream()
                        .collect(groupingBy(ReportEntryEntity::getDate,toList()));


        Map<String,Map<Date, Float>> appWithHoursPerDay = new HashMap<>();
        List<ApplicationEntity> apps = ApplicationEntity.getAllApplications();

        apps.forEach(app -> appWithHoursPerDay.put(app.getName(), new HashMap<>()));


        for(Map.Entry<Date,List<ReportEntryEntity>> e : entitiesGroupedByDate.entrySet()) {
            for(ReportEntryEntity val : e.getValue()){
                Date date = val.getDate();
                Map<Date, Float> appMap = appWithHoursPerDay.get(val.getApplicationName());

                Long end = val.getEndTime().getTime();
                Long start = val.getStartTime().getTime();
                Long diff = end - start;
                Float dur = (float) diff / (1000 * 60 * 60);

                if(appMap.get(date) != null){
                    appMap.put(date, appMap.get(date) + dur);
                } else {
                    appMap.put(date, dur);
                }
            }
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(String app : appWithHoursPerDay.keySet()){
            XYChart.Series series = new XYChart.Series();
            series.setName(app);
            ArrayList<Map.Entry<Date, Float>> hoursPerDay = new ArrayList<>(appWithHoursPerDay.get(app).entrySet());
            hoursPerDay.sort(Comparator.comparing(Map.Entry::getKey));
            hoursPerDay.forEach(entry -> {
                series.getData().add(new XYChart.Data<>(sdf.format(entry.getKey()), entry.getValue()));
            });
            chart.getData().add(series);
        }
        usageAxis.setLabel("Usage [H]");
    }

}