package com.management_system.utils;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.management_system.constant.ErrorCode;
import com.management_system.constant.MessageKey;

@Component
public class MessageUtil {
    private final MessageSource messageSource;

    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(ErrorCode code, Object[] args, Locale locale) {
        return messageSource.getMessage(code.name(), args, locale);
    }

    public String getMessage(MessageKey key, Object[] args, Locale locale) {
        return messageSource.getMessage(key.name(), args, locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    public String format(ErrorCode code, MessageKey target, Locale locale) {
        return getMessage(code, new Object[] { getMessage(target, null, locale) }, locale);
    }
}
