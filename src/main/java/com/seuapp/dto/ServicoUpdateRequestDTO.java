package com.seuapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicoUpdateRequestDTO {
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer duracaoEmMinutos;
}
