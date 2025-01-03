package com.ftpl.finfactor.reporting.scheduler;

import com.ftpl.finfactor.reporting.utility.QuartelyMDData;
import com.ftpl.finfactor.reporting.utility.QuartleyLAPSData;
import com.ftpl.finfactor.reporting.utility.WeeklyDFSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataScheduler {

    @Autowired
    private QuartleyLAPSData quartleyLAPSData;

    @Autowired
    private QuartelyMDData quartelyMDData;

    @Autowired
    private WeeklyDFSData weeklyDFSData;

    @Scheduled(cron = "#{@quartleyLAPSData.cronSchedule()}", zone = "Asia/Kolkata")
    public void executeMonthlyLAPSData(){
        quartleyLAPSData.run();
    }

    @Scheduled(cron = "#{@quartelyMDData.cronSchedule()}", zone = "${cron.timezone}")
    public void executeMonthlyMDData(){
        quartelyMDData.run();
    }

    @Scheduled(cron = "#{@weeklyDFSData.cronSchedule()}", zone = "${cron.timezone}")
    public void executeWeeklyDFSData(){
        weeklyDFSData.run();
    }
}
