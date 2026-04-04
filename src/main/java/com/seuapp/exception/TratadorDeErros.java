package com.seuapp.exception;

import com.seuapp.dto.ErroMensagemDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class TratadorDeErros {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroMensagemDTO> tratarErro404(EntityNotFoundException e) {
        ErroMensagemDTO erro = new ErroMensagemDTO();
        erro.setMensagem(e.getMessage());
        erro.setStatus(404);
        return ResponseEntity.status(404).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroMensagemDTO> tratarErro400(MethodArgumentNotValidException e) {
        var errosDoSpring = e.getFieldErrors();

        String mensagemAmigavel = "Infelizmente, ";

        for (var erroDoSpring : errosDoSpring) {
            String nomeDoCampo = erroDoSpring.getField();
            String mensagemDoSpring = erroDoSpring.getDefaultMessage();
            mensagemAmigavel += "O campo '" + nomeDoCampo + "' " + mensagemDoSpring + ". ";
        }

        ErroMensagemDTO erro = new ErroMensagemDTO();
        erro.setMensagem(mensagemAmigavel);
        erro.setStatus(400);

        return ResponseEntity.status(400).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroMensagemDTO> tratarErroDeIntegridade(DataIntegrityViolationException e) {
        ErroMensagemDTO erro = new ErroMensagemDTO();
        erro.setMensagem("Operação negada: Não é possível excluir este registro pois ele está vinculado a outros dados do sistema (ex: agendamentos).");
        erro.setStatus(409);
        return ResponseEntity.status(409).body(erro);
    }
}
