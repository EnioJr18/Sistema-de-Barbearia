package com.seuapp.dto;

import com.seuapp.model.Agendamento.FormaPagamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgendamentoCreateRequestDTO {
    private LocalDateTime dataEHora;
    private StatusAgendamento status;
    private FormaPagamento formaDePagamento;
    private Long clienteId;
    private Long barbeiroId;
    private Long servicoId;
    private ReferenciaRequestDTO cliente;
    private ReferenciaRequestDTO barbeiro;
    private ReferenciaRequestDTO servico;
}
