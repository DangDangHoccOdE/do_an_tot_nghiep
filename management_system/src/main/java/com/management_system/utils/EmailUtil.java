package com.management_system.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.management_system.service.inter.IEmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailUtil {
    @Value("${mail.username}")
    private String emailFrom;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final IEmailService iEmailService;

    public void sendEmailActive(String email, String activationCode) {
        String subject = "Kich hoat tai khoan cua ban tai OurApp";

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedCode = URLEncoder.encode(activationCode, StandardCharsets.UTF_8);
        String url = "%s/activate?email=%s&code=%s".formatted(frontendUrl, encodedEmail, encodedCode);

        String text = """
                <html>
                <body>
                    <p>Xin chao <b>%s</b>,</p>
                    <p>Vui long su dung ma sau de kich hoat tai khoan cua ban:</p>
                    <h2>%s</h2>
                    <p>Hoac click vao link duoi day de kich hoat (Thoi gian het han: <b>10 phut</b>):</p>
                    <a href="%s">%s</a>
                    <br/><br/>
                    <p>Tran trong,</p>
                    <p>OurApp Team</p>
                </body>
                </html>
                """.formatted(email, activationCode, url, url);

        iEmailService.sendMessage(emailFrom, email, subject, text);
    }

}
