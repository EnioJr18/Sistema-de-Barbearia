package com.seuapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataEHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaDePagamento;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false) // Nome único
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "barbeiro_id", nullable = false) // Nome único
    private Usuario barbeiro;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;


    public enum StatusAgendamento {
        PENDENTE, CONFIRMADO, CONCLUIDO, CANCELADO, NO_SHOW
    }

    public enum FormaPagamento {
        PIX, DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO
    }
}