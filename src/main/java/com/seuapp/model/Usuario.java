package com.seuapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_usuarios")
public class Usuario implements org.springframework.security.core.userdetails.UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String perfil;

    // --- MÉTODOS DO CONTRATO USERDETAILS (SPRING SECURITY) ---

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        if ("BARBEIRO".equals(perfil)) {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        else if ("CLIENTE".equals(perfil)) {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USUARIO"));
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

