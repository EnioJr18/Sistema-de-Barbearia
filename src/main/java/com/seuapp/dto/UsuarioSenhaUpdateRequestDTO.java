package com.seuapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioSenhaUpdateRequestDTO {
    @NotBlank(message = "senha e obrigatoria")
    @Size(min = 6, message = "senha deve ter no minimo 6 caracteres")
    private String senha;
}
