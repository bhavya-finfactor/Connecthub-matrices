package com.ftpl.finfactor.reporting.scheduler;

import com.ftpl.finfactor.reporting.utility.MonthlyLAPSData;
import com.ftpl.finfactor.reporting.utility.MonthlyMDData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataScheduler {

    @Autowired
    private MonthlyLAPSData monthlyLAPSData;

    @Autowired
    private MonthlyMDData monthlyMDData;

    @Scheduled(cron = "#{@monthlyLAPSData.cronSchedule()}")
    public void executeMonthlyLAPSData(){
        monthlyLAPSData.run();
    }

    @Scheduled(cron = "#{@monthlyMDData.cronSchedule()}")
    public void executeMonthlyMDData(){
        monthlyMDData.run();
    }
}
