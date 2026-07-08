package com.seuapp.exception;

import com.seuapp.dto.CampoErroDTO;
import com.seuapp.dto.ErroApiDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class TratadorDeErrosException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApiDTO> tratarErroDeValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<CampoErroDTO> fields = exception.getFieldErrors()
                .stream()
                .map(error -> new CampoErroDTO(error.getField(), error.getDefaultMessage()))
                .toList();

        ErroApiDTO erro = criarErro(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "Existem campos invalidos na requisicao.",
                request.getRequestURI());
        erro.setFields(fields);

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroApiDTO> tratarErro404(EntityNotFoundException exception, HttpServletRequest request) {
        return construirResposta(
                HttpStatus.NOT_FOUND,
                "Not Found",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroApiDTO> tratarErroDeRegraDeNegocio(
            ResponseStatusException exception,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() != null ? exception.getReason() : "Erro de regra de negocio.";

        return construirResposta(status, status.getReasonPhrase(), message, request.getRequestURI());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErroApiDTO> tratarErroDoSpring(
            ErrorResponseException exception,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getBody().getDetail() != null
                ? exception.getBody().getDetail()
                : "Requisicao invalida.";

        return construirResposta(status, status.getReasonPhrase(), message, request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroApiDTO> tratarErroDeIntegridade(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.CONFLICT,
                "Conflict",
                "Operacao negada: o registro esta vinculado a outros dados do sistema ou viola uma restricao de integridade.",
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApiDTO> tratarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Requisicao invalida.",
                request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroApiDTO> tratarAcessoNegado(AccessDeniedException exception, HttpServletRequest request) {
        return construirResposta(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Acesso negado.",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApiDTO> tratarErroInesperado(Exception exception, HttpServletRequest request) {
        return construirResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Erro interno inesperado.",
                request.getRequestURI());
    }

    private ResponseEntity<ErroApiDTO> construirResposta(
            HttpStatus status,
            String error,
            String message,
            String path) {

        return ResponseEntity
                .status(status)
                .body(criarErro(status, error, message, path));
    }

    private ErroApiDTO criarErro(HttpStatus status, String error, String message, String path) {
        ErroApiDTO erro = new ErroApiDTO();
        erro.setTimestamp(LocalDateTime.now());
        erro.setStatus(status.value());
        erro.setError(error);
        erro.setMessage(message);
        erro.setPath(path);
        return erro;
    }
}
