package com.seuapp.repository;

import com.seuapp.model.Agendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    Page<Agendamento> findByCliente_NomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Agendamento> findByBarbeiroId(Long barbeiroId, Pageable pageable);

    boolean existsByBarbeiroIdAndDataEHora(Long barbeiroId, java.time.LocalDateTime dataEHora);
}