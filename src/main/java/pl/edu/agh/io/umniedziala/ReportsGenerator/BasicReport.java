package pl.edu.agh.io.umniedziala.ReportsGenerator;

import com.opencsv.CSVWriter;

import javafx.util.Pair;
import pl.edu.agh.io.umniedziala.databaseUtilities.QuerryExecutor;
import pl.edu.agh.io.umniedziala.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static java.util.stream.Collectors.*;

public class BasicReport {

    File file;
    FileWriter outputFile;
    CSVWriter writer;
    LocalDate from;
    LocalDate to;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    public static String basicHeader = "date,start time,end time,duration";
    public static String extendedHeader = basicHeader+",app name,start,end";
    FilesOperations fileOperator;

    public BasicReport(LocalDate from, LocalDate to) throws IOException {
        try {
            String filePath = "./report_" + from.toString() + "_" + to.toString()+".csv";
            file = new File(filePath);
            outputFile = new FileWriter(file);
            writer = new CSVWriter(outputFile,  CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            fileOperator = new FilesOperations(writer);
            this.from = from;
            this.to = to;
        }
        catch(IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void createReportWithApps() {
        getReportWithApps(from.toString(),to.toString());
        fileOperator.close();
    }

    public void createReportWithoutApps() {
        getReportWithoutApps(from.toString(),to.toString());
        fileOperator.close();
    }

    public Map<Integer,Double> findActiveTime(Date date){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = Date.from(day.atStartOfDay().atZone(ZoneId.systemDefault())
//                .toInstant());

        String sDate = sdf.format(date);

        List<Period> results = new ArrayList<>();
        results.addAll(BackgroundPeriodEntity.findByStartDate(sDate));
        try {
            results.addAll(QuerryExecutor.getPeriodsForDay(date));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        results.addAll(ComputerRunningPeriodEntity.findByStartDate(sDate));
        results.addAll(CustomEventEntity.findByStartDate(sDate));

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat utc_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utc_sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Map<Integer, Double> appWithTimes = new HashMap<>();
        System.out.println(results.isEmpty());
        for (Period ent : results) {
            int seriesId = 0;
            if (ent instanceof AppPeriod)
                seriesId = ((AppPeriod) ent).getApplicationId();
            Integer idI = new Integer(seriesId);
            Double start = 0.0;
            Date startDate = null;
            Double length = 1.0;
            Double tmp = 0.0;
            try {
                Double end = (double) utc_sdf.parse(ent.getEndTime()).getTime() % 86400000 / 1000.0;
                startDate = sdf1.parse(ent.getStartTime());
                start = (double) utc_sdf.parse(ent.getStartTime()).getTime() % 86400000 / 1000.0;
                length = end - start; // czas w sekundach
            } catch (ParseException e) {
                e.printStackTrace();

            }
            start /= 3600.0;
            length /= 3600.0;

            if(!appWithTimes.containsKey(idI)){
                appWithTimes.put(idI,length);
            }else{
                tmp = appWithTimes.get(idI);
                tmp += length;
                appWithTimes.replace(idI,tmp);
            }
        }

       // appWithTimes.forEach((x,y) -> System.out.println(x + ": " + y));
        return appWithTimes;

    }

    public void getReportWithoutApps(String from, String to){

        TreeMap<Date,Pair<Date,Date>> timeIntervals = new TreeMap<>();
        TreeMap<Date,Double> activeTimeIntervals = new TreeMap<>();

        fileOperator.writeDateRangeOfReport(from, to);
        fileOperator.writeMetadata(String.format(basicHeader).split(","));

        List<ReportEntryEntity> entities = parseResultSet(ReportEntryEntity.getReportEntries(from,to));
        Map<Date,List<ReportEntryEntity>> entitiesGroupedByDate =
                entities.stream()
                        .collect(groupingBy(ReportEntryEntity::getDate,toList()));

        for(Map.Entry<Date,List<ReportEntryEntity>> e : entitiesGroupedByDate.entrySet()){

            Map<Integer, Double> apps = findActiveTime(e.getKey());
            double totalTime = 0.0;
            totalTime = apps.values().stream().reduce(0.0,Double::sum);

            ArrayList<Date> startTimes = new ArrayList();
            ArrayList<Date> endTimes = new ArrayList();
            for(ReportEntryEntity r : e.getValue()){
                startTimes.add(r.getStartTime());
                endTimes.add(r.getEndTime());
            }

            Date start = Collections.min(startTimes);
            Date end = Collections.max(endTimes);

            timeIntervals.put(e.getKey(),new Pair<>(start,end));
            activeTimeIntervals.put(e.getKey(),totalTime);

        }
        fileOperator.writeToFile(formatReportWithoutApps(timeIntervals,activeTimeIntervals));
    }

    public void getReportWithApps(String from, String to){

        TreeMap<Date, DayEntry> appsTimeIntervals = new TreeMap<>();


        fileOperator.writeDateRangeOfReport(from, to);
        fileOperator.writeMetadata(String.format(extendedHeader).split(","));

        List<ReportEntryEntity> entities = parseResultSet(ReportEntryEntity.getReportEntries(from,to));

        //findActiveTime();

        Map<Date,List<ReportEntryEntity>> entitiesGroupedByDate =
                entities.stream()
                        .collect(groupingBy(ReportEntryEntity::getDate,toList()));

        for(Map.Entry<Date,List<ReportEntryEntity>> e : entitiesGroupedByDate.entrySet()){

            ArrayList<Date> startTimes = new ArrayList();
            ArrayList<Date> endTimes = new ArrayList();
            Map<String,Pair<TreeSet<Date>,TreeSet<Date>>> apps = new TreeMap<>();
            Map<String,Pair<Date,Date>> appsWithTimes = new TreeMap<>();

            for(ReportEntryEntity r : e.getValue()){
                startTimes.add(r.getStartTime());
                endTimes.add(r.getEndTime());
                if(!apps.keySet().contains(r.getApplicationName())){
                    apps.put(r.getApplicationName(),new Pair<>(new TreeSet<>(),new TreeSet<>()));
                }
                apps.get(r.getApplicationName()).getValue().add(r.getStartTime());
                apps.get(r.getApplicationName()).getValue().add(r.getStartTime());
            }

            apps.forEach((x,y) -> appsWithTimes.put(x,new Pair<>(y.getValue().first(),y.getValue().last())));
            appsTimeIntervals.put(e.getKey(),new DayEntry(Collections.min(startTimes),Collections.max(endTimes),appsWithTimes));
        }

        fileOperator.writeToFile(formatReportWithAppps(appsTimeIntervals));
    }

    private List<String> formatReportWithoutApps(Map<Date,Pair<Date,Date>> entries, Map<Date,Double> active){

        List<String> timeIntervalsStrings = new ArrayList<>();

        for(Map.Entry<Date,Pair<Date,Date>> s : entries.entrySet()){

            timeIntervalsStrings.add(String.format("%s#%s#%s#%.1f",dateFormat.format(s.getKey()),
                    timeFormat.format(s.getValue().getKey()),
                    timeFormat.format(s.getValue().getValue()),
                    //findTimeDiff(s.getValue().getKey(),s.getValue().getValue())/60)
                    active.get(s.getKey()))
                    .replace(',','.'));
        }
        return timeIntervalsStrings;
    }

    private List<String> formatReportWithAppps(Map<Date,DayEntry> entries){

        List<String> timeIntervalsAndAppsString = new ArrayList<>();
        for(Map.Entry<Date, DayEntry> s : entries.entrySet()){

            String appsList = "";

            for (Map.Entry<String,Pair<Date,Date>> x : s.getValue().getApplicationsList().entrySet()){
                appsList = appsList+(String.format("%s#%s#%s#",x.getKey(),
                        timeFormat.format(x.getValue().getKey()),
                        timeFormat.format(x.getValue().getValue())));
            }

            timeIntervalsAndAppsString.add(String.format("%s#%s#%s#%.1f#%s",dateFormat.format(s.getKey()),
                    timeFormat.format(s.getValue().getStart()),
                    timeFormat.format(s.getValue().getEnd()),
                    findTimeDiff(s.getValue().getStart(),s.getValue().getEnd())/60,
                    appsList).replace(',','.'));
        }
        return timeIntervalsAndAppsString;
    }

    public static List<ReportEntryEntity> parseResultSet(ResultSet result){

        List<ReportEntryEntity> entities = new ArrayList<>();

        try {
            while(result.next()){
                ReportEntryEntity tmp = new ReportEntryEntity();

                tmp.setId(result.getInt("id"));
                tmp.setApplicationName(result.getString(result.findColumn("name")));
                tmp.setDate(dateFormat.parse(result.getString(result.findColumn("start_time")).split(" ")[0]));
                tmp.setStartTime(timeFormat.parse(result.getString(result.findColumn("start_time")).split(" ")[1]));
                tmp.setEndTime(timeFormat.parse(result.getString(result.findColumn("end_time")).split(" ")[1]));

                entities.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return entities;
    }

    private float findTimeDiff(Date start,Date end){

        long diffInMillies = Math.abs(end.getTime() - start.getTime());
        float diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff;

    }

    public class DayEntry{

        Date start;
        Date end;
        Map<String,Pair<Date,Date>> applicationsList;

        public DayEntry(Date start, Date end, Map<String,Pair<Date,Date>> applicationsList) {
            this.start = start;
            this.end = end;
            this.applicationsList = applicationsList;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public Map<String,Pair<Date,Date>> getApplicationsList() {
            return applicationsList;
        }
    }
}
