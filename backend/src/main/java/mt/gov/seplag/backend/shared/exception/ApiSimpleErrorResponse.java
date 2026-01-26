package mt.gov.seplag.backend.shared.exception;

import java.time.LocalDateTime;

public class ApiSimpleErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;

    public ApiSimpleErrorResponse(int status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}