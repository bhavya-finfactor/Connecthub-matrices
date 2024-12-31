package com.ftpl.finfactor.reporting.scheduler;

import com.ftpl.finfactor.reporting.utility.MonthlyLAPSData;
import com.ftpl.finfactor.reporting.utility.MonthlyMDData;
import com.ftpl.finfactor.reporting.utility.WeeklyDFSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataScheduler {

    @Autowired
    private MonthlyLAPSData monthlyLAPSData;

    @Autowired
    private MonthlyMDData monthlyMDData;

    @Autowired
    private WeeklyDFSData weeklyDFSData;

    @Scheduled(cron = "#{@monthlyLAPSData.cronSchedule()}", zone = "Asia/Kolkata")
    public void executeMonthlyLAPSData(){
        monthlyLAPSData.run();
    }

    @Scheduled(cron = "#{@monthlyMDData.cronSchedule()}", zone = "Asia/Kolkata")
    public void executeMonthlyMDData(){
        monthlyMDData.run();
    }

    @Scheduled(cron = "#{@weeklyDFSData.cronSchedule()}", zone = "Asia/Kolkata")
    public void executeWeeklyDFSData(){
        weeklyDFSData.run();
    }
}
