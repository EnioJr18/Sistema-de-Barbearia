package com.seuapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seuapp.model.Agendamento.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgendamentoCreateRequestDTO {
    @NotNull(message = "dataEHora e obrigatoria")
    private LocalDateTime dataEHora;

    @NotNull(message = "formaDePagamento e obrigatoria")
    private FormaPagamento formaDePagamento;

    @Positive(message = "clienteId deve ser maior que zero")
    private Long clienteId;

    @Positive(message = "barbeiroId deve ser maior que zero")
    private Long barbeiroId;

    @Positive(message = "servicoId deve ser maior que zero")
    private Long servicoId;

    @Valid
    private ReferenciaRequestDTO cliente;

    @Valid
    private ReferenciaRequestDTO barbeiro;

    @Valid
    private ReferenciaRequestDTO servico;

    @JsonIgnore
    @AssertTrue(message = "cliente e obrigatorio")
    public boolean isClienteInformado() {
        return clienteId != null || (cliente != null && cliente.getId() != null);
    }

    @JsonIgnore
    @AssertTrue(message = "barbeiro e obrigatorio")
    public boolean isBarbeiroInformado() {
        return barbeiroId != null || (barbeiro != null && barbeiro.getId() != null);
    }

    @JsonIgnore
    @AssertTrue(message = "servico e obrigatorio")
    public boolean isServicoInformado() {
        return servicoId != null || (servico != null && servico.getId() != null);
    }
}
