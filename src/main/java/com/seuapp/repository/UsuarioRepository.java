package com.seuapp.repository;

import com.seuapp.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    org.springframework.security.core.userdetails.UserDetails findByEmail(String email);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}