package com.ftpl.finfactor.reporting.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public abstract class ReportingTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportingTask.class);

    protected ReportType getReportType() {
        return null;
    }

    public abstract Serializable fetchData();

    public abstract void triggerReport(Serializable data) throws Exception;

    public abstract String cronScheduler();

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            Serializable data = fetchData();
            logger.info("Data fetch for jobType={} complete in {}ms", getReportType(), System.currentTimeMillis() - startTime);

            if (data == null || (data instanceof List && ((List<?>) data).isEmpty())) {
                logger.info("No data available for reportType={}", getReportType());
                return;
            }

            triggerReport(data);
            logger.info("Email sent successfully for jobType={}", getReportType());
        } catch (Exception e) {
            logger.error("Failed to process jobType={} due to error: {}", getReportType(), e.getMessage(), e);
        }
    }

}
