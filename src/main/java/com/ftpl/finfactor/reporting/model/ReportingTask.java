package com.ftpl.finfactor.reporting.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class ReportingTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportingTask.class);

    public abstract Serializable fetchData();

    public abstract void triggerReport(Serializable data) throws Exception;

    public abstract String cronScheduler();

    @Override
    public void run() {
        try {
            Serializable data = fetchData();
            if (data == null) {
                logger.warn("No data retrieved. The report generation process will not proceed.");
                return;
            }
            triggerReport(data);
        } catch (Exception e) {
            e.getMessage();
        }
    }

}
