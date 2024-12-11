package com.ftpl.connecthub.matrics.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${email.recipients}")
    private String recipients;

    @Value("${email.subject}")
    private String subject;

    @Value("${email.signature}")
    private String signature;

    @Value("${query.data.fetch}")
    private String query;

    public void sendEmailWithData(LocalDate startDate, LocalDate endDate) throws MessagingException {
        List<Map<String, Object>> data = fetchData(startDate, endDate);

        String emailBody = prepareEmailBody(data);

        sendEmail(recipients, subject, emailBody);
    }

    private List<Map<String, Object>> fetchData(LocalDate startDate, LocalDate endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return jdbcTemplate.queryForList(query, params);
    }

    private String prepareEmailBody(List<Map<String, Object>> data) {
        StringBuilder body = new StringBuilder("<h1>Monthly Report</h1><table border='1'><tr><th>Column1</th><th>Column2</th></tr>");
        for (Map<String, Object> row : data) {
            body.append("<tr><td>")
                    .append(row.get("column1"))
                    .append("</td><td>")
                    .append(row.get("column2"))
                    .append("</td></tr>");
        }
        body.append("</table>");
        return body.toString();
    }

    private void sendEmail(String recipient, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }
}
