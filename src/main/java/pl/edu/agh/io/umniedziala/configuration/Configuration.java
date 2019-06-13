package pl.edu.agh.io.umniedziala.configuration;

import com.moandjiezana.toml.Toml;
import pl.edu.agh.io.umniedziala.model.ApplicationEntity;

import java.util.*;
import java.util.stream.Collectors;

public class Configuration {
    private static Configuration instance;

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
            instance.read();
        }
        return instance;
    }

    private Long checkInterval = 0L;
    private Long inactivityPeriod = 0L;
    private Long chartStart = 0L;
    private Long chartEnd = 0L;
    private List<ApplicationEntity> monitoredApplications = new LinkedList<>();

    private Configuration() {
        GeneralConfigurationManager.getInstance();
    }

    private void read() {
        Toml config = GeneralConfigurationManager.getInstance().config();

        checkInterval = config.getLong("monitor.check_interval", 500L);
        inactivityPeriod = config.getLong("monitor.inactivity_period", 15L);
        chartStart = config.getLong("monitor.chart.start", 0L);
        chartEnd = config.getLong("monitor.chart.end", 23L);

        monitoredApplications = readMonitoredApplications(config);
    }

    private List<ApplicationEntity> readMonitoredApplications(Toml config) {
        List<List<String>> input = config.getList("monitor.applications");

        return input.stream()
                .filter(s -> s.size() >= 3)
                .map(
                        s -> {
                            Optional<ApplicationEntity> app = ApplicationEntity.findByName(s.get(0));
                            return app.orElseGet(() -> ApplicationEntity.create(s.get(0), s.get(1), s.get(2)).get());
                        }
                ).collect(Collectors.toList());
    }

    public void resetToDefaults() {
        GeneralConfigurationManager.getInstance().resetToDefaults();
        this.read();
    }

    public Long getCheckInterval() {
        return checkInterval;
    }

    public Long getInactivityPeriod() {
        return inactivityPeriod;
    }

    public Long getChartStart() {
        return chartStart;
    }

    public Long getChartEnd() {
        return chartEnd;
    }

    public void setCheckInterval(Long checkInterval) {
        this.checkInterval = checkInterval;

        Toml config = GeneralConfigurationManager.getInstance().config();

        Map<String, Object> map = config.toMap();

        ((HashMap<String, Object>)map.get("monitor")).put("check_interval", checkInterval);

        GeneralConfigurationManager.getInstance().setConfigurationFromMap(map);

        this.read();
    }

    public void setInactivityPeriod(Long inactivityPeriod) {
        this.inactivityPeriod = inactivityPeriod;

        Toml config = GeneralConfigurationManager.getInstance().config();

        Map<String, Object> map = config.toMap();

        ((HashMap<String, Object>)map.get("monitor")).put("inactivity_period", inactivityPeriod);


        GeneralConfigurationManager.getInstance().setConfigurationFromMap(map);

        this.read();
    }

    public void setChartStart(Long chartStart) {
        this.chartStart = chartStart;

        Toml config = GeneralConfigurationManager.getInstance().config();

        Map<String, Object> map = config.toMap();

        ((HashMap<String, Object>)((HashMap<String, Object>)map.get("monitor")).get("chart")).put("start", chartStart);

        GeneralConfigurationManager.getInstance().setConfigurationFromMap(map);

        this.read();
    }

    public void setChartEnd(Long chartEnd) {
        this.chartEnd = chartEnd;

        Toml config = GeneralConfigurationManager.getInstance().config();

        Map<String, Object> map = config.toMap();
        ((HashMap<String, Object>)((HashMap<String, Object>)map.get("monitor")).get("chart")).put("end", chartEnd);

        GeneralConfigurationManager.getInstance().setConfigurationFromMap(map);

        this.read();
    }

    public List<ApplicationEntity> getMonitoredApplications() {
        return monitoredApplications;
    }

    public void setMonitoredApplications(List<ApplicationEntity> monitoredApplications) {
        this.monitoredApplications = monitoredApplications;

        Toml config = GeneralConfigurationManager.getInstance().config();

        Map<String, Object> map = config.toMap();

        List<List<String>> serialized = monitoredApplications.stream()
                .map(
                        a -> Arrays.asList(a.getName(), a.getApplicationPath(), a.getColor()))
                .collect(Collectors.toList());

        ((HashMap<String, Object>) map.get("monitor")).put("applications", serialized);

        GeneralConfigurationManager.getInstance().setConfigurationFromMap(map);

        this.read();
    }
}
