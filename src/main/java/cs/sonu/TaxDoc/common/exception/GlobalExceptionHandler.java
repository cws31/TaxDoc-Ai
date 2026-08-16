package cs.sonu.TaxDoc.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
                problem.setTitle("Resource Not Found");
                problem.setType(URI.create("https://taxdoc.cs.sonu/errors/not-found"));
                problem.setProperty("timestamp", Instant.now());
                return problem;
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
                problem.setTitle("Bad Request");
                problem.setProperty("timestamp", Instant.now());
                return problem;
        }

        @ExceptionHandler(AiProcessingException.class)
        public ProblemDetail handleAiError(AiProcessingException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
                problem.setTitle("AI Processing Error");
                problem.setProperty("timestamp", Instant.now());
                return problem;
        }

        @ExceptionHandler(StorageException.class)
        public ProblemDetail handleStorageError(StorageException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                                ex.getMessage());
                problem.setTitle("Storage Error");
                problem.setProperty("timestamp", Instant.now());
                return problem;
        }
}