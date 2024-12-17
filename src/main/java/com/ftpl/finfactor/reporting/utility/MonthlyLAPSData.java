package com.ftpl.finfactor.reporting.utility;

import com.ftpl.finfactor.reporting.Model.ReportingTask;
import com.ftpl.finfactor.reporting.Service.EmailService;
import com.ftpl.finfactor.reporting.dao.MonthlyLAPSDataDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MonthlyLAPSData extends ReportingTask {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyConnecthubData.class);

    @Autowired
    private MonthlyLAPSDataDAO monthlyLAPSDataDAO;

    @Autowired
    private EmailService emailService;

    @Override
    public Serializable fetchData() {

        LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Map<String, Object>> finsenseData = monthlyLAPSDataDAO.fetchFinsenseRequestStatusCount(startDate,endDate);

        List<Map<String, Object>> pfmData = monthlyLAPSDataDAO.fetchPfmRequestStatusCount(startDate, endDate);

        List<Map<String, Object>> combinedData = new ArrayList<>();
        combinedData.addAll(finsenseData);
        combinedData.addAll(pfmData);


        logger.info("Fetched {} rows for reportType={}", combinedData.size(), getReportType());
        return new ArrayList<>(combinedData);
    }

    @Override
    public void triggerReport(Serializable data) throws Exception {
        if (data instanceof List && !((List<?>) data).isEmpty()) {
            List<String> recipients = List.of("support@finfactor.in");
            String subject = "Monthly LAPSData Report";
            emailService.sendEmail(recipients, subject, data.toString());
        } else {
            logger.info("No valid data to send for reportType={}", getReportType());
        }
    }

    @Override
    public String cronScheduler() {
        return "0 0 9 1 * ?";
    }
}
