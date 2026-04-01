package com.seuapp.repository;

import com.seuapp.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByBarbeiroId(Long barbeiroId);

    List<Agendamento> findByClienteId(Long clienteId);

    boolean existsByBarbeiroIdAndDataEHora(Long barbeiroId, java.time.LocalDateTime dataEHora);
}