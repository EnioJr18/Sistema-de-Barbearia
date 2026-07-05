package com.seuapp.dto;

import lombok.Data;

@Data
public class UsuarioCreateRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private String perfil;
}
