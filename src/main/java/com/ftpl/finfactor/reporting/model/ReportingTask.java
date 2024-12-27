package com.ftpl.finfactor.reporting.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class ReportingTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportingTask.class);

    public abstract ReportType getReportType();

    public abstract Serializable fetchData();

    public abstract void triggerReport(Serializable data) throws Exception;

    public abstract String cronSchedule();

    @Override
    public void run() {

        logger.info("Starting report generation for ReportType={}", getReportType());
        long startTime = System.currentTimeMillis();

        Serializable data;

        try {
            data = fetchData();
            if (data == null) {
                logger.warn("No data retrieved. The report generation process will not proceed.");
                return;
            }
            long timeTaken = System.currentTimeMillis() - startTime;
            logger.info("Data fetched successfully for ReportType={} in {} ms", getReportType(), timeTaken);
        }catch (Exception e) {
            logger.error("Error occurred while fetching data for report type {}: {}", getReportType(), e.getMessage());
            return;
        }

        try{
            triggerReport(data);
            logger.info("Report generated successfully for ReportType={}", getReportType());
        } catch (Exception e) {
            logger.error("Error occurred while sending report for report type {}: {}", getReportType(), e.getMessage());
        }
    }

}
