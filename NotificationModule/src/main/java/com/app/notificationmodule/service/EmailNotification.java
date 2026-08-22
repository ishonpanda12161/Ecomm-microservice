package com.app.notificationmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotification implements SendNotification{

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String to, String subject, String body) {
        try{
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mail.setFrom("info@test-86org8e1ekzgew13.mlsender.net");
            javaMailSender.send(mail);
        }catch (Exception e)
        {
            log.error("Couldn't send MAIL to {}. Exception: {}", to, e.getMessage(), e);
        }
    }
}
