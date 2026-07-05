package com.seuapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCreateRequestDTO {
    @NotBlank(message = "nome e obrigatorio")
    private String nome;

    @NotBlank(message = "email e obrigatorio")
    @Email(message = "deve ser um endereco de e-mail valido")
    private String email;

    @NotBlank(message = "senha e obrigatoria")
    @Size(min = 6, message = "senha deve ter no minimo 6 caracteres")
    private String senha;

    @NotBlank(message = "perfil e obrigatorio")
    private String perfil;
}
