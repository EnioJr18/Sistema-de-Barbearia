package com.seuapp.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgendamentoResponseDTO {
    private Long id;
    private LocalDateTime dataEHora;
    private String nomeCliente;
    private String nomeServico;
}
