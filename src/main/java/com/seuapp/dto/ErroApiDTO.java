package com.seuapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Resposta padrao de erro da API.")
public class ErroApiDTO {
    @Schema(description = "Data e hora em que o erro ocorreu", example = "2026-07-05T00:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "Codigo HTTP", example = "400")
    private Integer status;

    @Schema(description = "Descricao curta do status HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem de erro amigavel", example = "Requisicao invalida.")
    private String message;

    @Schema(description = "Caminho da requisicao", example = "/agendamentos")
    private String path;

    @Schema(description = "Lista de campos invalidos quando houver erro de validacao")
    private List<CampoErroDTO> fields;
}
