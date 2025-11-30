package com.management_system.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {

    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

}


