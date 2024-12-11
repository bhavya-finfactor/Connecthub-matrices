package com.ftpl.connecthub.matrics.Controller;

import com.ftpl.connecthub.matrics.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class controller {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-email")
    public String sendEmail(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            emailService.sendEmailWithData(startDate, endDate);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


}
