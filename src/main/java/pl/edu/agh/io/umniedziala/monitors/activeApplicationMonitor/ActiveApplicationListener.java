package pl.edu.agh.io.umniedziala.monitors.activeApplicationMonitor;

import pl.edu.agh.io.umniedziala.configuration.Configuration;
import pl.edu.agh.io.umniedziala.windowsHandlers.WindowsFunctionHandler;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActiveApplicationListener extends Thread {
    private static final Logger logger = Logger.getLogger(ActiveApplicationListener.class.getName());

    private final ApplicationRunningPeriodsManager programRunningPeriodsManager;
    private int checkingIntervalInMs;
    private volatile boolean exit = false;


    public ActiveApplicationListener(int checkingIntervalInMs) {

        this.programRunningPeriodsManager = new ApplicationRunningPeriodsManager();
        this.checkingIntervalInMs = checkingIntervalInMs;
        Configuration.getInstance().getMonitoredApplications();
    }

    public void run() {
        logger.info("Running active application listener");
        while (!exit) {
            Optional<String> windowName = WindowsFunctionHandler.getCurrentActiveWindowName();

            if (windowName.isPresent()) {
                String winName = windowName.get();

                String appName = winName.split("\\\\")[winName.split("\\\\").length - 1];

                logger.info("Active window title: " + winName);

                this.programRunningPeriodsManager.handleApplicationRunningPeriod(appName);
            } else {
                logger.log(Level.WARNING, "Active window not found");
            }

            try {
                Thread.sleep(checkingIntervalInMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Stopping active application listener");
    }

    public void stopListening() {
        this.exit = true;
    }
    }
