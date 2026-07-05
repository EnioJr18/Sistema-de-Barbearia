package com.seuapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AutenticacaoDTO {
    @NotBlank(message = "email e obrigatorio")
    @Email(message = "deve ser um endereco de e-mail valido")
    private String email;

    @NotBlank(message = "senha e obrigatoria")
    private String senha;
}
