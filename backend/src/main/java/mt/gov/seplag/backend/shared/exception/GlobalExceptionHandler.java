package mt.gov.seplag.backend.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNotFound(
                NotFoundException ex,
                HttpServletRequest request
        ) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        404,
                        "Recurso não encontrado",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiErrorResponse> handleBusiness(
                BusinessException ex,
                HttpServletRequest request
        ) {
                return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        400,
                        "Erro de negócio",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
        }

        @ExceptionHandler(FileValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleFileValidation(
                FileValidationException ex,
                HttpServletRequest request
        ) {
                return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        400,
                        "Erro de validação de arquivo",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiErrorResponse> handleUnauthorized(
                UnauthorizedException ex,
                HttpServletRequest request
        ) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        401,
                        "Não autorizado",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ApiErrorResponse> handleForbidden(
                ForbiddenException ex,
                HttpServletRequest request
        ) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        403,
                        "Acesso negado",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                MethodArgumentNotValidException ex,
                HttpServletRequest request
        ) {
                List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

                return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        400,
                        "Erro de validação",
                        errors,
                        request.getRequestURI()
                ));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneric(
                Exception ex,
                HttpServletRequest request
        ) {
                ex.printStackTrace(); // Log para debug
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        500,
                        "Erro interno",
                        List.of(ex.getMessage() != null ? ex.getMessage() : "Erro inesperado no servidor"),
                        request.getRequestURI()
                ));
        }
}