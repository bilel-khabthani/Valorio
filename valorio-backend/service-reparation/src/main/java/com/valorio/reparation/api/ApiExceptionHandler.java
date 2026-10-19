package com.valorio.reparation.api;

import com.valorio.reparation.ai.security.AccesInterditException;
import com.valorio.reparation.ai.security.AccesNonAutoriseException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail notFound(NoSuchElementException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Requête invalide", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .reduce((a, b) -> a + " ; " + b)
                .orElse("Requête invalide");
        return problem(HttpStatus.BAD_REQUEST, "Validation échouée", detail, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail conflit(IllegalStateException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Opération impossible", ex.getMessage(), request);
    }

    @ExceptionHandler(AccesNonAutoriseException.class)
    public ProblemDetail nonAutorise(AccesNonAutoriseException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentification requise", ex.getMessage(), request);
    }

    @ExceptionHandler(AccesInterditException.class)
    public ProblemDetail interdit(AccesInterditException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Accès interdit", ex.getMessage(), request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
