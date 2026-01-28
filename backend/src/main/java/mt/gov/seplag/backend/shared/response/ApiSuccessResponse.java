package mt.gov.seplag.backend.shared.response;

import java.time.LocalDateTime;

public class ApiSuccessResponse<T> {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;
    private String path;

    public ApiSuccessResponse(int status, String message, T data, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.data = data;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getPath() {
        return path;
    }
    
    public String getMessage() {
        return message;
    }
}