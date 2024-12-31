package com.ftpl.finfactor.reporting.utility;

import com.ftpl.finfactor.reporting.configuration.ThymeleafTemplateConfig;
import com.ftpl.finfactor.reporting.dao.WeeklyDFSDataDAO;
import com.ftpl.finfactor.reporting.model.DFSDataCount;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ftpl.finfactor.reporting.model.ReportType.WEEKLY_DFS_REPORT;

@Component
public class WeeklyDFSData extends ReportingTask {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyDFSData.class);

    @Autowired
    private WeeklyDFSDataDAO weeklyDFSDataDAO;

    @Autowired
    private ThymeleafTemplateConfig thymeleafTemplateConfig;

    @Autowired
    private EmailService emailService;

    @Value("${cron.weekly.dfs.data}")
    private String cronExpression;

    @Value("${report.dfs.email.recipients}")
    private List<String> emailRecipients;


    @Override
    public ReportType getReportType() {
        return WEEKLY_DFS_REPORT;
    }

    @Override
    public Serializable fetchData() {

        LocalDate startDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(4);


        List<DFSDataCount> fiData = weeklyDFSDataDAO.fetchFICounts(startDate,endDate);

        List<DFSDataCount> consenseData = weeklyDFSDataDAO.fetchConsentCounts(startDate, endDate);

        logger.info("Fetched {} rows for FiData and {} rows for Consent data for reportType={}",
                fiData.size(), consenseData.size(), getReportType());

        WeeklyDFSData.CombinedDFSData combinedData = new WeeklyDFSData.CombinedDFSData(fiData, consenseData);

        return combinedData;
    }

    @Override
    public void triggerReport(Serializable data) throws Exception {
        if (!(data instanceof CombinedDFSData combinedData)) {
            logger.warn("Invalid data type provided for report generation.");
            return;
        }

        LocalDate startDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(4);


        Map<String, Integer> fiCounts = computeCounts(combinedData.fiDataCounts());

        Map<String, Integer> consentCounts = computeCounts(combinedData.consentDataCounts());

        int fiDataReadyCount = fiCounts.getOrDefault("200", 0);
        int fiDataFailedCount = fiCounts.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("200"))
                .mapToInt(Map.Entry::getValue)
                .sum();


        int totalficounts = fiDataReadyCount + fiDataFailedCount;
        int readyCountConsentData = consentCounts.getOrDefault("COMPLETED", 0);
        int failedCountConsentData = consentCounts.getOrDefault("FAILED", 0);
        int pendingNullCountConsentData =consentCounts.getOrDefault("PENDING", 0) + consentCounts.getOrDefault(null, 0);

        int totalCountconsent = readyCountConsentData + failedCountConsentData + pendingNullCountConsentData;

        Map<String, Double> avgTimes = weeklyDFSDataDAO.fetchAvgTime(startDate, endDate);

        // Calculate combined average time
        double combinedAverageTime = avgTimes.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Context emailData = new Context();
        emailData.setVariable("avgTimeOfDisposal", String.format("%.2f",combinedAverageTime));
        emailData.setVariable("fiReqReceivedFromAA", totalCountconsent);
        emailData.setVariable("fiNotificationByFIP", totalCountconsent);
        emailData.setVariable("fiFetchByAA", fiDataReadyCount);

        // Load the template and format the data
        String formattedData = thymeleafTemplateConfig.springTemplateEngine().process("dfsDataTemplate.html", emailData);

        try {
            // send email
            emailService.sendEmail(emailRecipients, "Weekly DFS Data Report", formattedData);
            logger.info("Email successfully sent for  reportType: {}", getReportType());
        } catch (Exception e) {
            logger.error("Failed to send email. Error: {}", e.getMessage(), e);
        }
    }

    @Override
    public String cronSchedule() {
        return cronExpression;
//                "30 1 1 * * ?";
    }

    private record CombinedDFSData(List<DFSDataCount> fiDataCounts, List<DFSDataCount> consentDataCounts) implements Serializable {
    }

    private Map<String, Integer> computeCounts(List<DFSDataCount> dataList) {
        Map<String, Integer> counts = new HashMap<>();
        for (DFSDataCount data : dataList) {
            counts.merge(data.status(), Integer.parseInt(data.count()), Integer::sum);
        }
        return counts;
    }
}
