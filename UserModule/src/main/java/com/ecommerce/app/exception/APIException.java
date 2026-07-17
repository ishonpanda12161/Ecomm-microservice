package com.ecommerce.app.exception;

import java.time.LocalDateTime;

public class APIException extends RuntimeException {

    String exceptionMessage;
    String type;
    LocalDateTime timestamp;

    public APIException(String exceptionMessage,String type,LocalDateTime timestamp) {
        super(timestamp+" - Exception Type: "+type+". Message: "+exceptionMessage);
        this.exceptionMessage = exceptionMessage;
        this.type = type;
        this.timestamp = timestamp;
    }
}
