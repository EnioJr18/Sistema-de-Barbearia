package com.seuapp.dto;

import lombok.Data;

@Data
public class UsuarioUpdateRequestDTO {
    private String nome;
    private String email;
    private String perfil;
}
