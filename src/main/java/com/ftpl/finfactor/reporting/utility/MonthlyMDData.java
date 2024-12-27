package com.ftpl.finfactor.reporting.utility;

import com.ftpl.finfactor.reporting.configuration.ThymeleafTemplateConfig;
import com.ftpl.finfactor.reporting.dao.MDDataReportDAO;
import com.ftpl.finfactor.reporting.model.MDDataCount;
import com.ftpl.finfactor.reporting.model.ReportType;
import com.ftpl.finfactor.reporting.model.ReportingTask;
import com.ftpl.finfactor.reporting.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ftpl.finfactor.reporting.model.ReportType.MONTHLY_MD_DATA_REPORT;

@Component
public class MonthlyMDData extends ReportingTask {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyMDData.class);

    @Autowired
    private MDDataReportDAO monthlyMDDataDao;

    @Autowired
    private ThymeleafTemplateConfig thymeleafTemplateConfig;

    @Autowired
    private EmailService emailService;

    @Value("${report.md.email.recipients}")
    private List<String> emailRecipients;

    LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    @Override
    public ReportType getReportType() {
        return MONTHLY_MD_DATA_REPORT;
    }

    @Override
    public Serializable fetchData() {
        List<MDDataCount> mdData = monthlyMDDataDao.fetchMDData(startDate,endDate);
        logger.info("Fetched {} rows for MD Data for reportType={}", mdData.size(), getReportType());

        System.out.println("data MD no"+mdData);
        return (Serializable) mdData;
    }

    @Override
    public void triggerReport(Serializable data) throws Exception {
        if (!(data instanceof List<?>)) {
            logger.warn("Invalid data type provided for report generation.");
            return;
        }

        List<MDDataCount> mdDataCountList = (List<MDDataCount>) data;
        
        Map<String, Integer> mdDataCounts = computeCounts(mdDataCountList);

        // Calculate differences
        int readyCount = mdDataCounts.getOrDefault("COMPLETED", 0);
        int failedCount = mdDataCounts.getOrDefault("FAILED", 0);
        int pendingNullCount = mdDataCounts.getOrDefault("PENDING", 0) + mdDataCounts.getOrDefault(null, 0);
        int totalCount = readyCount + failedCount + pendingNullCount;

        Context emailData = new Context();
        emailData.setVariable("startDate", startDate);
        emailData.setVariable("endDate", endDate);
        emailData.setVariable("totalTransactions", totalCount);
        emailData.setVariable("successfulTransactions", readyCount);
        emailData.setVariable("technicalDeclines", failedCount);
        emailData.setVariable("pending", pendingNullCount);

        // Load the template and format the data
        String formattedData = thymeleafTemplateConfig.springTemplateEngine().process("mdDataTemplate.html",emailData);

        try {
            // send email
            emailService.sendEmail(emailRecipients, "Monthly MD Data Report", formattedData);
            logger.info("Email successfully sent for  reportType: {}", getReportType());
        } catch (Exception e) {
            logger.error("Failed to send email. Error: {}", e.getMessage(), e);
        }
    }

    private Map<String, Integer> computeCounts(List<MDDataCount> dataList) {
        Map<String, Integer> counts = new HashMap<>();
        for (MDDataCount data : dataList) {
            counts.merge(data.status(), Integer.parseInt(data.count()), Integer::sum);
        }
        return counts;
    }

    @Override
    public String cronSchedule() {
        return "0 * * * * *";
    }
}
