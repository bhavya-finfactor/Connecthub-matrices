package com.ftpl.finfactor.reporting.controller;

import com.ftpl.finfactor.reporting.utility.MonthlyLAPSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ReportController {


    @Autowired
    MonthlyLAPSData monthlyLAPSData;

    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport( @RequestParam(value = "reportType") String reportType) {

        Map<String, Object> response = new HashMap<>();
        try {
            if (reportType == null || reportType.trim().isEmpty()) {
                response.put("error", "Invalid reportType. Please provide a valid report type.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Generate Report Data
            generateReportData(reportType);
            response.put("emailStatus", "Email sent successfully!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Error while generating the report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void generateReportData(String reportType) throws Exception {

        switch (reportType) {

            case "lapsdatareport":
                Serializable lapsData = monthlyLAPSData.fetchData();
                monthlyLAPSData.triggerReport(lapsData);
                break;

            default:
                throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
    }

}
