package com.ftpl.finfactor.reporting.controller;

import com.ftpl.finfactor.reporting.model.ReportType;
import com.ftpl.finfactor.reporting.model.ReportingTask;
import com.ftpl.finfactor.reporting.utility.QuartelyMDData;
import com.ftpl.finfactor.reporting.utility.QuartleyLAPSData;
import com.ftpl.finfactor.reporting.utility.WeeklyDFSData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/v1/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    QuartleyLAPSData quartleyLAPSData;

    @Autowired
    QuartelyMDData quartelyMDData;

    @Autowired
    WeeklyDFSData weeklyDFSData;


    @PostMapping("/{reportType}/trigger")
    public ResponseEntity<Map<String, Object>> generateReport( @PathVariable("reportType") ReportType reportType) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Generate Report Data
            logger.debug("Fetching the Data for reportType: {}",reportType);
            ReportingTask reportingTask = getTaskForReportType(reportType);

            logger.info("Execution of Data Report for reportType: {}",reportType);
            reportingTask.run();

            response.put("emailStatus", "Email sent successfully!");
            logger.info("Report generation completed successfully for reportType: {}", reportType);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error while generating the report for reportType: {}. Error: {}", reportType, e.getMessage(), e);
            response.put("error", "Error while generating the report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ReportingTask getTaskForReportType(ReportType reportType) throws Exception {

        switch (reportType) {
            case LAPS_DATA_REPORT:
                return quartleyLAPSData;

            case MD_DATA_REPORT:
                return quartelyMDData;

            case WEEKLY_DFS_REPORT:
                return weeklyDFSData;

            default:
                throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
    }

}
