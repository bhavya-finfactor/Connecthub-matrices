package com.ftpl.finfactor.reporting.utility;

import com.ftpl.finfactor.reporting.model.LAPSDataCount;
import com.ftpl.finfactor.reporting.model.ReportType;
import com.ftpl.finfactor.reporting.model.ReportingTask;
import com.ftpl.finfactor.reporting.service.EmailService;
import com.ftpl.finfactor.reporting.configuration.ThymeleafTemplateConfig;
import com.ftpl.finfactor.reporting.dao.MonthlyLAPSDataDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Component
public class MonthlyLAPSData extends ReportingTask {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyLAPSData.class);

    @Autowired
    private MonthlyLAPSDataDAO monthlyLAPSDataDAO;

    @Autowired
    private ThymeleafTemplateConfig thymeleafTemplateConfig;

    @Autowired
    private EmailService emailService;

    @Value("${report.laps.email.recipients}")
    private List<String> emailRecipients;

    LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());


    @Override
    public Serializable fetchData() {

        List<LAPSDataCount> finsenseData = monthlyLAPSDataDAO.fetchFinsenseStatusCount(startDate,endDate);

        List<LAPSDataCount> pfmData = monthlyLAPSDataDAO.fetchPfmStatusCount(startDate, endDate);

        logger.info("Fetched {} rows for Finsense and {} rows for PFM data for reportType={}",
                finsenseData.size(), pfmData.size(), ReportType.MONTHLY_LAPS_DATA_REPORT);

        CombinedLAPSData combinedData = new CombinedLAPSData(finsenseData, pfmData);

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
        emailData.setVariable("startDate", startDate);
        emailData.setVariable("endDate", endDate);
        emailData.setVariable("totalTransactions", totalCount);
        emailData.setVariable("successfulTransactions", readyCount);
        emailData.setVariable("technicalDeclines", failedCount);
        emailData.setVariable("pending", pendingNullCount);

        // Load the template and format the data
        String formattedData = thymeleafTemplateConfig.springTemplateEngine().process("lapsDataTemplate.html",emailData);

        // send email
        emailService.sendEmail(emailRecipients, "Monthly LAPS Data Report", formattedData);
    }

    public Map<String, Integer> computeCounts(List<LAPSDataCount> dataList) {
        Map<String, Integer> counts = new HashMap<>();
        for (LAPSDataCount data : dataList) {
            counts.merge(data.status(), Integer.parseInt(data.count()), Integer::sum);
        }
        return counts;
    }

    @Override
    public String cronScheduler() {
        return "0 0 9 1 * ?";
    }


    public record CombinedLAPSData(List<LAPSDataCount> finsenseData, List<LAPSDataCount> pfmData) implements Serializable {
    }
}
