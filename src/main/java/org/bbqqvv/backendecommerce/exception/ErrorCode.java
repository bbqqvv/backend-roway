package org.bbqqvv.backendecommerce.exception;

import org.springframework.http.HttpStatusCode;

public interface ErrorCode {
    int getCode();
    String getMessage();
    HttpStatusCode getStatusCode();
}
