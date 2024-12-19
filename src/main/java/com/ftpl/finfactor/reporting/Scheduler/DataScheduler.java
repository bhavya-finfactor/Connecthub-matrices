package com.ftpl.finfactor.reporting.Scheduler;

import com.ftpl.finfactor.reporting.utility.MonthlyLAPSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataScheduler {

    @Autowired
    private MonthlyLAPSData monthlyLAPSData;


    @Scheduled(cron = "#{@monthlyLAPSData.cronScheduler()}")
    public void executeMonthlyLAPSData(){
        monthlyLAPSData.run();
    }
}
