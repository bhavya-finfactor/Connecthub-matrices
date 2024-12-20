package com.ftpl.finfactor.reporting.utility;

import com.ftpl.finfactor.reporting.Model.LAPSDataCount;
import com.ftpl.finfactor.reporting.Model.ReportingTask;
import com.ftpl.finfactor.reporting.Service.EmailService;
import com.ftpl.finfactor.reporting.configuration.ThymeleafTemplateConfig;
import com.ftpl.finfactor.reporting.dao.MonthlyLAPSDataDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
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

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${report.email.recipients}")
    private String emailRecipients;

    @Value("${report.email.subject}")
    private String emailSubject;

    @Value("classpath:/templates/emailTemplate.html")
    private Resource emailBodyTemplate;

    @Override
    public Serializable fetchData() {

        LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<LAPSDataCount> finsenseData = monthlyLAPSDataDAO.fetchFinsenseStatusCount(startDate,endDate);

        List<LAPSDataCount> pfmData = monthlyLAPSDataDAO.fetchPfmStatusCount(startDate, endDate);

        logger.info("Fetched {} rows for Finsense and {} rows for PFM data for reportType={}",
                finsenseData.size(), pfmData.size(), getReportType());

        CombinedLAPSData combinedData = new CombinedLAPSData(finsenseData, pfmData);

        return combinedData;
    }

    @Override
    public void triggerReport(Serializable data) throws Exception {

        Context context = new Context();

        if (data instanceof CombinedLAPSData combinedData) {

            Map<String, Integer> finsenseCounts = computeCounts(combinedData.getFinsenseData());
            Map<String, Integer> pfmCounts = computeCounts(combinedData.getPfmData());


            // Calculate differences
            int readyCount = finsenseCounts.getOrDefault("READY", 0) - pfmCounts.getOrDefault("READY", 0);
            int failedCount = finsenseCounts.getOrDefault("FAILED", 0) - pfmCounts.getOrDefault("FAILED", 0);
            int pendingNullCount =
                    finsenseCounts.getOrDefault("PENDING", 0) + finsenseCounts.getOrDefault("null", 0)
                            - (pfmCounts.getOrDefault("PENDING", 0) + pfmCounts.getOrDefault("null", 0));
            int totalCount = readyCount + failedCount + pendingNullCount;

            Context emailData = new Context();
            emailData.setVariable("totalTransactions", totalCount);
            emailData.setVariable("successfulTransactions", readyCount);
            emailData.setVariable("technicalDeclines", failedCount);
            emailData.setVariable("pending", pendingNullCount);

            // Load the template and format the data
            String formattedData = thymeleafTemplateConfig.springTemplateEngine().process("emailTemplate.html",emailData);

            // send email
            List<String> recipients = Arrays.asList(emailRecipients.split(","));

            emailService.sendEmail(recipients, emailSubject, formattedData);
        } else {
            logger.info("No valid data to send for reportType={}", getReportType());
        }
    }

    public Map<String, Integer> computeCounts(List<LAPSDataCount> dataList) {
        Map<String, Integer> counts = new HashMap<>();
        for (LAPSDataCount data : dataList) {
            counts.merge(data.getStatus(), Integer.parseInt(data.getCount()), Integer::sum);
        }
        return counts;
    }

    @Override
    public String cronScheduler() {
        return "0 0 9 1 * ?";
    }


    public static class CombinedLAPSData implements Serializable {
        private final List<LAPSDataCount> finsenseData;
        private final List<LAPSDataCount> pfmData;

        public CombinedLAPSData(List<LAPSDataCount> finsenseData, List<LAPSDataCount> pfmData) {
            this.finsenseData = finsenseData;
            this.pfmData = pfmData;
        }

        public List<LAPSDataCount> getFinsenseData() {
            return finsenseData;
        }

        public List<LAPSDataCount> getPfmData() {
            return pfmData;
        }

        @Override
        public String toString() {
            return "CombinedLAPSData{" +
                    "finsenseData=" + finsenseData +
                    ", pfmData=" + pfmData +
                    '}';
        }
    }
}
