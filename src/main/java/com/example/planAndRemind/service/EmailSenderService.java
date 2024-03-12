package com.example.planAndRemind.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String toEmail, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cristianbadic@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
        System.out.println("Mail Send...");
    }

    public void sendEmailToMultipleUsers(List<String> toEmails, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cristianbadic@gmail.com");
        message.setTo(toEmails.toArray(new String[0]));
        message.setText(body);
        message.setSubject(subject);
        if (toEmails.size() > 0) {
            mailSender.send(message);
        }
        System.out.println("Mail Send...");
    }

    public String generateRegistrationCode() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // default registration code length
        int codeLength = 8;

        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int idx = 0; idx < codeLength; idx++) {
            int index = random.nextInt(characters.length());
            char character = characters.charAt(index);
            codeBuilder.append(character);
        }

        return codeBuilder.toString();
    }

    public void sendConfirmRegistrationEmail(String toEmail, String registrationCode) {

        String subject = "Plan & Remind: Confirm Registration";
        String body = "Hello! Te registration code for your account is the following: " + registrationCode;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cristianbadic@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
        System.out.println("Mail Send...");
    }

}
