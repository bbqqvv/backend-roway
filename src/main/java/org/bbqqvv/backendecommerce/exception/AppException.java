package org.bbqqvv.backendecommerce.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public AppException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.message = formatMessage(errorCode.getMessage(), args);
    }

    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i].toString());
        }
        return message;
    }
}
