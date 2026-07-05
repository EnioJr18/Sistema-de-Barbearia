package com.seuapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicoResumoResponseDTO {
    private Long id;
    private String nome;
    private BigDecimal preco;
}
