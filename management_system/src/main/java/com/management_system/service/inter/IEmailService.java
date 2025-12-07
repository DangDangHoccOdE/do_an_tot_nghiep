package com.management_system.service.inter;

public interface IEmailService {
    void sendMessage(String from, String to, String subject, String text);
}
