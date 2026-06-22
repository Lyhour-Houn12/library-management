package com.library.application.service;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
}
