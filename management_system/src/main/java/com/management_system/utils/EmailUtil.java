package com.management_system.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.management_system.service.inter.IEmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailUtil {
    @Value("${mail.username}")
    private String emailFrom;

    private final IEmailService iEmailService;

    public void sendEmailActive(String email, String activationCode) {
        String subject = "Kích hoạt tài khoản của bạn tại OurApp";

        String url = "http://localhost:5671/activatedAccount/" + email + "/" + activationCode;

        String text = """
                <html>
                <body>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Vui lòng sử dụng mã sau để kích hoạt tài khoản của bạn:</p>
                    <h2>%s</h2>
                    <p>Hoặc click vào link dưới đây để kích hoạt (Thời gian hết hạn: <b>10 phút</b>):</p>
                    <a href="%s">%s</a>
                    <br/><br/>
                    <p>Trân trọng,</p>
                    <p>OurApp Team</p>
                </body>
                </html>
                """.formatted(email, activationCode, url, url);

        iEmailService.sendMessage(emailFrom, email, subject, text);
    }

}
