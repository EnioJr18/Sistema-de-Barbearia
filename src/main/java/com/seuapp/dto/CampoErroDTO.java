package com.seuapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Detalhe de erro de validacao em um campo.")
public class CampoErroDTO {
    @Schema(description = "Nome do campo invalido", example = "email")
    private String field;

    @Schema(description = "Mensagem de validacao", example = "deve ser um endereco de e-mail valido")
    private String message;
}
