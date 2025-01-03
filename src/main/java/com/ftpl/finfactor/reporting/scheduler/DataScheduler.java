package com.ftpl.finfactor.reporting.scheduler;

import com.ftpl.finfactor.reporting.utility.QuarterlyLAPSData;
import com.ftpl.finfactor.reporting.utility.QuarterlyMDData;
import com.ftpl.finfactor.reporting.utility.WeeklyDFSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataScheduler {

    @Autowired
    private QuarterlyLAPSData quarterlyLAPSData;

    @Autowired
    private QuarterlyMDData quarterlyMDData;

    @Autowired
    private WeeklyDFSData weeklyDFSData;

    @Scheduled(cron = "#{@quarterlyLAPSData.cronSchedule()}", zone = "Asia/Kolkata")
    public void executeMonthlyLAPSData(){
        quarterlyLAPSData.run();
    }

    @Scheduled(cron = "#{@quarterlyMDData.cronSchedule()}", zone = "${cron.timezone}")
    public void executeMonthlyMDData(){
        quarterlyMDData.run();
    }

    @Scheduled(cron = "#{@weeklyDFSData.cronSchedule()}", zone = "${cron.timezone}")
    public void executeWeeklyDFSData(){
        weeklyDFSData.run();
    }
}
