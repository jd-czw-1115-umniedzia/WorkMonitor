package pl.edu.agh.io.umniedziala.view;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import pl.edu.agh.io.umniedziala.configuration.Configuration;
import pl.edu.agh.io.umniedziala.model.AppPeriod;
import pl.edu.agh.io.umniedziala.model.CustomEventEntity;
import pl.edu.agh.io.umniedziala.model.Period;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeChart extends XYChart<Number, String> {

    // granice w których wyświetlamy wyrkes
    private int minHour;
    private int maxHour;

    private double lineHeight = 60.0;

    // YAxis zawiera stringi: TODO zamienić to na inty i formatować na stringi korzystając z appNames
    private Map<Integer, String> appNames;
    private Map<Integer, XYChart.Series> seriesMap = new HashMap<>();

    static class ExtraData {
        double length;
        String style;

        ExtraData(double length, String style) {
            super();
            this.length = length;
            this.style = style;
        }
    }

    static class CustomExtraData extends ExtraData {

        String name;
        String desc;
        Date start;
        int id;

        CustomExtraData(double length, Date start, CustomEventEntity c) {
            // length i start są przekazywane, a nie wybierane i obliczane z CustomEventEntity bo są już
            // policzone gdzie indziej i tak jest wygodnie
            super(length, c.getColor());
            this.name = c.getName();
            this.desc = c.getDescription();
            this.start = start;
            this.id = c.getId();
        }
    }

    public TimeChart(@NamedArg("xAxis") NumberAxis timeAxis,
                     @NamedArg("yAxis") CategoryAxis appAxis) {
        super(timeAxis, appAxis);
        Configuration config = Configuration.getInstance();
        minHour = Math.toIntExact(config.getChartStart());
        maxHour = Math.toIntExact(config.getChartEnd());
        setData(FXCollections.observableArrayList());
        timeAxis.setLowerBound(minHour);
        setLegendVisible(false);

        // oś czasu przechowuje godziny dnia ( double 0-24) i wyświetla je po formatowaniu do stringa
        // wczesniej przechowywalismy sekundy ale tak Ticki beda sie przemieszczac z przesuwaniem
        timeAxis.setUpperBound(maxHour);
        timeAxis.setTickUnit(1.0);

        timeAxis.setTickLabelFont(Font.font(14));
        appAxis.setTickLabelFont(Font.font(16));

        timeAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // koniecznie UTC. tylko do formatowania!
                long value = (long) (object.doubleValue() * 3600.0 * 1000.0);
                // zamiana s na ms
                return sdf.format(new Date(value));
            }

            @Override
            public Number fromString(String string) {
                return 0L;
            }
        });

        final double[] lastMouseX = {0};

        setOnMousePressed(event -> lastMouseX[0] = event.getX());

        setOnMouseDragged(event -> {
            event.consume();
            double pastMinHour = timeAxis.getLowerBound();
            double pastMaxHour = timeAxis.getUpperBound();

            double delta = (lastMouseX[0] - event.getX()) / timeAxis.getWidth() * (maxHour - minHour);
            if (pastMinHour + delta < 0)
                delta = -1 * pastMinHour;
            else if (pastMaxHour + delta > 24) {
                delta = -1 * pastMaxHour + 24;
            }
            timeAxis.setLowerBound(pastMinHour + delta);
            timeAxis.setUpperBound(pastMaxHour + delta);

            lastMouseX[0] = event.getX();
        });
    }

    private XYChart.Series addNewApp(Integer id, String name) {
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        seriesMap.put(id, series);
        getData().add(series);
        return series;
    }

    public void setAppNames(Map<Integer, String> appNames) {
        getData().clear();
        this.appNames = appNames;
        for (Map.Entry<Integer, String> ent : appNames.entrySet()) {
            addNewApp(ent.getKey(), ent.getValue());
        }
        lineHeight = getYAxis().getHeight() / (appNames.size() + 1);

        getYAxis().setVisible(false);
        getYAxis().setVisible(true);
        getYAxis().setAutoRanging(true);
    }

    public void setDataByResults(List<Period> results) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat utc_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utc_sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (Series series : seriesMap.values()) {
            series.getData().clear();
        }

        for (Period ent : results) {
            int seriesId = 0;
            if (ent instanceof AppPeriod)
                seriesId = ((AppPeriod) ent).getApplicationId();
            XYChart.Series series = seriesMap.get(seriesId);
            Double start = 0.0;
            Date startDate = null;
            Double length = 1.0;
            try {
                Double end = (double) utc_sdf.parse(ent.getEndTime()).getTime() % 86400000 / 1000.0;
                startDate = sdf.parse(ent.getStartTime());
                start = (double) utc_sdf.parse(ent.getStartTime()).getTime() % 86400000 / 1000.0;
                length = end - start; // czas w sekundach
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
            start /= 3600.0;
            length /= 3600.0;
            String appName = appNames.get(seriesId);
            ExtraData ed;
            if (ent instanceof CustomEventEntity) {
                CustomEventEntity c = (CustomEventEntity) ent;
                ed = new CustomExtraData(length, startDate, c);
            } else {
                ed = new ExtraData(length, ent.getColor());
            }
            series.getData().add(new XYChart.Data<Number, String>(start, appName, ed));
        }
    }

    @Override
    protected void layoutPlotChildren() {

        for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {

            Series<Number, String> series = getData().get(seriesIndex);

            Iterator<Data<Number, String>> iter = getDisplayedDataIterator(series);
            while (iter.hasNext()) {
                Data<Number, String> item = iter.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node node = item.getNode();
                Rectangle box;
                if (node instanceof StackPane) {
                    StackPane region = (StackPane) item.getNode();

                    double boxLineHeight = lineHeight;
                    // custom eventy są trochę mniejsze niż aktywność, żeby dało się zobaczyć aktywnośc
                    ExtraData extra = (ExtraData) item.getExtraValue();
                    if (extra instanceof CustomExtraData) {
                        boxLineHeight *= 0.7;
                    }

                    if (region.getShape() == null) {
                        box = new Rectangle(((ExtraData) item.getExtraValue()).length, boxLineHeight);
                    } else if (region.getShape() instanceof Rectangle) {
                        box = (Rectangle) region.getShape();
                    } else {
                        return;
                    }
                    box.setWidth(extra.length * Math.abs(((NumberAxis) getXAxis()).getScale()));
                    box.setHeight(boxLineHeight);
                    y -= boxLineHeight / 2.0;

                    region.setShape(null);
                    region.setShape(box);
                    region.setScaleShape(false);
                    region.setCenterShape(false);
                    region.setCacheShape(false);

                    node.setLayoutX(x);
                    node.setLayoutY(y);

                    if (extra instanceof CustomExtraData)
                        // to chyba powinno być przeniesione do Controllera ale co tam
                        node.setOnMousePressed(e -> {
                                    CustomExtraData cextra = (CustomExtraData) extra;
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                    SimpleDateFormat utc_sdf = new SimpleDateFormat("HH:mm");
                                    utc_sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    String time = utc_sdf.format(new java.util.Date((long) (cextra.length*60*60*1000)));
                                    ButtonType deletDis = new ButtonType("Delete");
                                    Date end = new Date((long) (cextra.start.getTime() + cextra.length*60*60*1000));
                                    String content = "Opis: " + cextra.desc + "\n\n" + "Od: " + sdf.format(cextra.start)
                                            + "\nDo: " + sdf.format(end)
                                            + "\nCzas trwania: " + time;
                                    Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK, deletDis);
                                    a.setHeaderText(cextra.name);
                                    a.setTitle("Custom event");
                                    Optional<ButtonType> option = a.showAndWait();
                                    if (option.isPresent() && option.get() == deletDis) {
                                        try {
                                            CustomEventEntity.delete(cextra.id);
                                            cextra.length = 0; // xd. Gdyby to był kontroler to możnaby uruchomić refreshChart() ale tak w sumie jest szybciej
                                            layoutPlotChildren();
                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                            new Alert(Alert.AlertType.ERROR, "Nie udało się usunąć custom eventa: "
                                                    + ex.getMessage(), ButtonType.OK).showAndWait();
                                        }
                                    }
                                }
                        );
                }
            }
        }
    }

    @Override
    protected void dataItemAdded(Series<Number, String> series, int itemIndex, Data<Number, String> item) {
        Node block = createContainer(series, getData().indexOf(series), item, itemIndex);
        getPlotChildren().add(block);
    }

    @Override
    protected void dataItemRemoved(final Data<Number, String> item, final Series<Number, String> series) {
        final Node block = item.getNode();
        getPlotChildren().remove(block);
        removeDataItemFromDisplay(series, item);
    }

    @Override
    protected void dataItemChanged(Data<Number, String> item) {
        // nie wiem co robić
    }

    @Override
    protected void seriesAdded(Series<Number, String> series, int seriesIndex) {
        for (int j = 0; j < series.getData().size(); j++) {
            Data<Number, String> item = series.getData().get(j);
            Node container = createContainer(series, seriesIndex, item, j);
            getPlotChildren().add(container);
        }
    }

    @Override
    protected void seriesRemoved(final Series<Number, String> series) {
        for (XYChart.Data<Number, String> d : series.getData()) {
            final Node container = d.getNode();
            getPlotChildren().remove(container);
        }
        removeSeriesFromDisplay(series);
    }


    private Node createContainer(Series<Number, String> series, int seriesIndex, final Data<Number, String> item, int itemIndex) {

        Node container = item.getNode();

        if (container == null) {
            container = new StackPane();
            item.setNode(container);
        }

        // TODO: nie wybierania kolorów jeszcze. Wszystko jest różowe
        String style = ((ExtraData) item.getExtraValue()).style;
        /*
        int red = (int) (style.getRed() * 255);
        int green = (int) (style.getGreen() * 255);
        int blue = (int) (style.getBlue() * 255);
        String cssValue = String.format("-fx-background-color: rgba(%d, %d, %d, %f)", red, green, blue, style.getOpacity());
         */

        String cssValue = "-fx-background-color: " + style;
        container.setStyle(cssValue);

        return container;
    }

    @Override
    protected void updateAxisRange() {
        final Axis<Number> xa = getXAxis();
        final Axis<String> ya = getYAxis();
        List<Number> xData = null;
        List<String> yData = null;
        if (xa.isAutoRanging()) xData = new ArrayList<Number>();
        if (ya.isAutoRanging()) yData = new ArrayList<String>();
        if (xData != null || yData != null) {
            for (Series<Number, String> series : getData()) {
                for (Data<Number, String> data : series.getData()) {
                    if (xData != null) {
                        xData.add(data.getXValue());
                        xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) + ((ExtraData) data.getExtraValue()).length));
                    }
                    if (yData != null) {
                        yData.add(data.getYValue());
                    }
                }
            }
            if (xData != null) xa.invalidateRange(xData);
            if (yData != null) ya.invalidateRange(yData);
        }
    }

}