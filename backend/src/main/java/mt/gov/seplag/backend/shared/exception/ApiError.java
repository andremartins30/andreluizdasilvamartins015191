package mt.gov.seplag.backend.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ApiError {

    private int status;
    private String error;
    private List<String> messages;
    private String path;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiError(int status, String error, List<String> messages, String path) {
        this.status = status;
        this.error = error;
        this.messages = messages;
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
