package com.ftpl.finfactor.reporting.Scheduler;

import com.ftpl.finfactor.reporting.utility.MonthlyConnecthubData;
import com.ftpl.finfactor.reporting.utility.MonthlyLAPSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataScheduler {

    @Autowired
    private MonthlyConnecthubData monthlyConnecthubData;

    @Autowired
    private MonthlyLAPSData monthlyLAPSData;

    @Scheduled(cron = "#{@monthlyConnecthubData.cronScheduler()}")
    public void executeMonthlyConnecthubData(){
        monthlyConnecthubData.run();
    }

    @Scheduled(cron = "#{@monthlyLAPSData.cronScheduler()}")
    public void executeMonthlyLAPSData(){
        monthlyLAPSData.run();
    }
}
