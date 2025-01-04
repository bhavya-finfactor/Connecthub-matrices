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
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ftpl.finfactor.reporting.model.ReportType.MD_DATA_REPORT;

@Component
public class QuarterlyMDData extends ReportingTask {

    private static final Logger logger = LoggerFactory.getLogger(QuarterlyMDData.class);

    @Autowired
    private MDDataReportDAO mdDataReportDAO;

    @Autowired
    private ThymeleafTemplateConfig thymeleafTemplateConfig;

    @Autowired
    private EmailService emailService;

    @Value("${report.md.email.recipients}")
    private List<String> emailRecipients;

    @Value("${cron.md.data}")
    private String cronExpression;


    @Override
    public ReportType getReportType() {
        return MD_DATA_REPORT;
    }

    @Override
    public Serializable fetchData() {

        LocalDate lastMonthDate = LocalDate.now().minusMonths(1);
        LocalDate firstDayOfQuarter = lastMonthDate.with(lastMonthDate.getMonth().firstMonthOfQuarter()).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfLastMonth = lastMonthDate.with(TemporalAdjusters.lastDayOfMonth());

        List<MDDataCount> mdData = mdDataReportDAO.fetchMDData(firstDayOfQuarter,lastDayOfLastMonth);
        logger.info("Fetched {} rows for MD Data for reportType={}", mdData.size(), getReportType());

        GetMDData combinedData = new QuarterlyMDData.GetMDData(mdData, firstDayOfQuarter, lastDayOfLastMonth);

        return  combinedData;
    }

    @Override
    public void triggerReport(Serializable data) throws Exception {

        if (!(data instanceof QuarterlyMDData.GetMDData mdData)) {
            logger.warn("Invalid data type provided for report generation.");
            return;
        }

        Map<String, Integer> mdDataCounts = computeCounts(mdData.mdDataCountList());

        // Calculate differences
        int readyCount = mdDataCounts.getOrDefault("COMPLETED", 0);
        int failedCount = mdDataCounts.getOrDefault("FAILED", 0);
        int pendingNullCount = mdDataCounts.getOrDefault("PENDING", 0) + mdDataCounts.getOrDefault(null, 0);
        int totalCount = readyCount + failedCount + pendingNullCount;

        Context emailData = new Context();
        emailData.setVariable("startDate", mdData.startDate);
        emailData.setVariable("endDate", mdData.endDate);
        emailData.setVariable("totalTransactions", totalCount);
        emailData.setVariable("successfulTransactions", readyCount);
        emailData.setVariable("technicalDeclines", failedCount);
        emailData.setVariable("pending", pendingNullCount);

        // Load the template and format the data
        String formattedData = thymeleafTemplateConfig.springTemplateEngine().process("mdDataTemplate.html",emailData);

            // send email
            emailService.sendEmail(emailRecipients, "FIP Data Report", formattedData);
            logger.info("Email successfully sent for  reportType: {}", getReportType());
    }

    private Map<String, Integer> computeCounts(List<MDDataCount> dataList) {
        Map<String, Integer> counts = new HashMap<>();
        for (MDDataCount data : dataList) {
            counts.merge(data.status(), Integer.parseInt(data.count()), Integer::sum);
        }
        return counts;
    }

    public record GetMDData(List<MDDataCount> mdDataCountList, LocalDate startDate, LocalDate endDate) implements Serializable {
    }

    @Override
    public String cronSchedule() {
        return cronExpression;
    }
}
