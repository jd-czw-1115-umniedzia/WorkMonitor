package pl.edu.agh.io.umniedziala.monitors.backgroundApplicationsMonitor;

import pl.edu.agh.io.umniedziala.model.ApplicationEntity;
import pl.edu.agh.io.umniedziala.model.BackgroundPeriodEntity;
import pl.edu.agh.io.umniedziala.model.RunningPeriodEntity;
import pl.edu.agh.io.umniedziala.windowsHandlers.WindowsFunctionHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackgroundApplicationsMonitor extends Thread {

    private volatile boolean exit = false;

    private int checkingIntervalInMs;

    private List<ApplicationEntity> applicationEntityList;

                    //app_id : back_id
    private HashMap<Integer, Integer> entitiesMap = new HashMap<>(); //temporary

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public BackgroundApplicationsMonitor(int checkingIntervalInMs){
        this.applicationEntityList = new ArrayList<>();
        this.checkingIntervalInMs = checkingIntervalInMs;
    }

    private void initValues(){
        applicationEntityList = ApplicationEntity.getAllApplications();
        for(ApplicationEntity applicationEntity : applicationEntityList){
            String start = dateFormat.format(new Date());
            int id = BackgroundPeriodEntity.create(start,start,applicationEntity.getId()).get().getId();
            entitiesMap.put(applicationEntity.getId(), id);
        }
    }

    public void run(){

        initValues();

        while(!exit){

            Map<String, String> processes = WindowsFunctionHandler.getAllRunningProcesses();

            for(ApplicationEntity applicationEntity : ApplicationEntity.getAllApplications()){
                if(processes.containsKey(applicationEntity.getName())){
                    applicationEntityList.add(applicationEntity);
                }
            }

            applicationEntityList.remove(ApplicationEntity.findByName(WindowsFunctionHandler.getCurrentActiveWindowName().orElse("")));

            for(ApplicationEntity applicationEntity : applicationEntityList){

                int id = applicationEntity.getId();
                if(BackgroundPeriodEntity.findById(entitiesMap.get(id)).isPresent()){
                    BackgroundPeriodEntity.update(entitiesMap.get(id), BackgroundPeriodEntity.findById(entitiesMap.get(id)).get().getStartTime(), dateFormat.format(new Date()));
                } else {
                    String start = dateFormat.format(new Date());
                    BackgroundPeriodEntity.create(start,start,id);
                }
            }

            applicationEntityList.clear();

            try{
                Thread.sleep(checkingIntervalInMs);
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }

        }

    }

}
