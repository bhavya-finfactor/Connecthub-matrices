package com.ftpl.finfactor.reporting.utility;

import com.ftpl.finfactor.reporting.model.LAPSDataCount;
import com.ftpl.finfactor.reporting.model.ReportType;
import com.ftpl.finfactor.reporting.model.ReportingTask;
import com.ftpl.finfactor.reporting.service.EmailService;
import com.ftpl.finfactor.reporting.configuration.ThymeleafTemplateConfig;
import com.ftpl.finfactor.reporting.dao.LAPSDataDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static com.ftpl.finfactor.reporting.model.ReportType.LAPS_DATA_REPORT;

@Component
public class QuarterlyLAPSData extends ReportingTask {

    private static final Logger logger = LoggerFactory.getLogger(QuarterlyLAPSData.class);

    @Autowired
    private LAPSDataDAO lapsDataDAO;

    @Autowired
    private ThymeleafTemplateConfig thymeleafTemplateConfig;

    @Autowired
    private EmailService emailService;

    @Value("${cron.laps.data}")
    private String cronExpression;


    @Value("${report.laps.email.recipients}")
    private List<String> emailRecipients;



    @Override
    public ReportType getReportType() {
        return LAPS_DATA_REPORT;
    }

    @Override
    public Serializable fetchData() {

        LocalDate lastMonthDate = LocalDate.now().minusMonths(1);
        LocalDate firstDayOfQuarter = lastMonthDate.with(lastMonthDate.getMonth().firstMonthOfQuarter()).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfLastMonth = lastMonthDate.with(TemporalAdjusters.lastDayOfMonth());

        List<LAPSDataCount> finsenseData = lapsDataDAO.fetchFinsenseStatusCount(firstDayOfQuarter,lastDayOfLastMonth);

        List<LAPSDataCount> pfmData = lapsDataDAO.fetchPfmStatusCount(firstDayOfQuarter, lastDayOfLastMonth);

        logger.info("Fetched {} rows for Finsense and {} rows for PFM data for reportType={}",
                finsenseData.size(), pfmData.size(), getReportType());

        CombinedLAPSData combinedData = new CombinedLAPSData(finsenseData, pfmData, firstDayOfQuarter, lastDayOfLastMonth);

        return combinedData;
    }

    @Override
    public void triggerReport(Serializable data) throws Exception {

        if (!(data instanceof CombinedLAPSData combinedData)) {
            logger.warn("Invalid data type provided for report generation.");
            return;
        }

        Map<String, Integer> finsenseCounts = computeCounts(combinedData.finsenseData());
        Map<String, Integer> pfmCounts = computeCounts(combinedData.pfmData());

        // Calculate differences
        int readyCount = finsenseCounts.getOrDefault("READY", 0) - pfmCounts.getOrDefault("READY", 0);
        int failedCount = finsenseCounts.getOrDefault("FAILED", 0) - pfmCounts.getOrDefault("FAILED", 0);
        int pendingNullCount =
                finsenseCounts.getOrDefault("PENDING", 0) + finsenseCounts.getOrDefault(null, 0)
                        - (pfmCounts.getOrDefault("PENDING", 0) + pfmCounts.getOrDefault(null, 0));
        int totalCount = readyCount + failedCount + pendingNullCount;

        Context emailData = new Context();
        emailData.setVariable("startDate", combinedData.startDate);
        emailData.setVariable("endDate", combinedData.endDate);
        emailData.setVariable("totalTransactions", totalCount);
        emailData.setVariable("successfulTransactions", readyCount);
        emailData.setVariable("technicalDeclines", failedCount);
        emailData.setVariable("pending", pendingNullCount);

        // Load the template and format the data
        String formattedData = thymeleafTemplateConfig.springTemplateEngine().process("lapsDataTemplate.html", emailData);

            // send email
            emailService.sendEmail(emailRecipients, "LAPS Data Report", formattedData);
            logger.info("Email successfully sent for  reportType: {}", getReportType());
    }

    public Map<String, Integer> computeCounts(List<LAPSDataCount> dataList) {
        Map<String, Integer> counts = new HashMap<>();
        for (LAPSDataCount data : dataList) {
            counts.merge(data.status(), Integer.parseInt(data.count()), Integer::sum);
        }
        return counts;
    }


    @Override
    public String cronSchedule() {
        return cronExpression;
    }


    public record CombinedLAPSData(List<LAPSDataCount> finsenseData, List<LAPSDataCount> pfmData,LocalDate startDate, LocalDate endDate) implements Serializable {
    }
}
