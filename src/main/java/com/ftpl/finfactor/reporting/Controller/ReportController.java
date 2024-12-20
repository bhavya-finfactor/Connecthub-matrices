package com.ftpl.finfactor.reporting.Controller;

import com.ftpl.finfactor.reporting.Model.LAPSDataCount;
import com.ftpl.finfactor.reporting.Service.EmailService;
import com.ftpl.finfactor.reporting.utility.MonthlyLAPSData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MonthlyLAPSData monthlyLAPSData;

    @GetMapping("/send-email")
    public String sendMonthlyLAPSData(
            @RequestParam(value = "sendEmail", defaultValue = "false") boolean sendEmail) {
        Map<String, Object> response = new HashMap<>();
        try {
            //  Fetch Data
            Serializable data = monthlyLAPSData.fetchData();

            if (data instanceof MonthlyLAPSData.CombinedLAPSData combinedData) {
                //  Process Data for the Response
                List<LAPSDataCount> fsData = filterData(combinedData.getFinsenseData());
                List<LAPSDataCount> pfmData = filterData(combinedData.getPfmData());

                // Process Data for the Response
                Map<String, Integer> finsenseCounts = monthlyLAPSData.computeCounts(fsData);
                Map<String, Integer> pfmCounts = monthlyLAPSData.computeCounts(pfmData);

                if (sendEmail) {
                    //  Trigger the Report if sendEmail is true
                    monthlyLAPSData.triggerReport(data);
                    response.put("emailStatus", "Email sent successfully!");
                } else {
                    response.put("emailStatus", "Email not sent (sendEmail=false).");
                }
            } else {
                response.put("error", "No valid data found to generate the report.");
            }
        } catch (Exception e) {
            response.put("error", "Error while generating the report: " + e.getMessage());
        }
        return response.toString();
    }

    private List<LAPSDataCount> filterData(List<LAPSDataCount> dataList) {
        if (dataList == null) {
            return Collections.emptyList();
        }
        return dataList.stream()
                .filter(data -> data.getStatus() != null && data.getCount() != null)
                .filter(data -> {
                    try {
                        Integer.parseInt(data.getCount()); // Ensure count is a valid number
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .toList();
    }

}
