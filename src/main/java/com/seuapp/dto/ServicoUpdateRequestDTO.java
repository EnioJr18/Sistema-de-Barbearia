package com.seuapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicoUpdateRequestDTO {
    @NotBlank(message = "nome e obrigatorio")
    private String nome;
    private String descricao;

    @NotNull(message = "preco e obrigatorio")
    @Positive(message = "preco deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "duracaoEmMinutos e obrigatoria")
    @Positive(message = "duracaoEmMinutos deve ser maior que zero")
    private Integer duracaoEmMinutos;
}
