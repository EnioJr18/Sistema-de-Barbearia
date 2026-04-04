package com.seuapp.repository;

import com.seuapp.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Page<Servico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}