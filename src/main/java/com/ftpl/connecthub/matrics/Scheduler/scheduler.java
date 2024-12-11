package com.ftpl.connecthub.matrics.Scheduler;

import com.ftpl.connecthub.matrics.Service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class scheduler {

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 10 1 * ?")
    public void runMonthlyJob() {
        try {
            LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            emailService.sendEmailWithData(startDate, endDate);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
