package com.seuapp.repository;

import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    Page<Agendamento> findByCliente_NomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Agendamento> findByBarbeiroId(Long barbeiroId, Pageable pageable);

    boolean existsByBarbeiroIdAndDataEHora(Long barbeiroId, java.time.LocalDateTime dataEHora);

    List<Agendamento> findByBarbeiroIdAndStatusNot(Long barbeiroId, StatusAgendamento status);
}
