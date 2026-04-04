package com.seuapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicoResponseDTO {
    private String nome;
    private Long id;
    private String descricao;
    private BigDecimal preco;
    private Integer duracaoEmMinutos;
}
