package mt.gov.seplag.backend.shared.exception;

import java.util.List;
import java.time.LocalDateTime;

public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private List<FieldErrorResponse> errors;

    public ApiErrorResponse(int status, List<FieldErrorResponse> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.errors = errors;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public List<FieldErrorResponse> getErrors() {
        return errors;
    }

}