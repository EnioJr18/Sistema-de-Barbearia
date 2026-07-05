package com.seuapp.config;

import com.seuapp.model.Usuario;
import com.seuapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminBootstrap implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dev-admin.enabled:false}")
    private boolean enabled;

    @Value("${app.dev-admin.nome:Admin Dev}")
    private String nome;

    @Value("${app.dev-admin.email:}")
    private String email;

    @Value("${app.dev-admin.senha:}")
    private String senha;

    @Override
    public void run(String... args) {
        if (!enabled || email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return;
        }

        if (usuarioRepository.findByEmail(email) != null) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(nome);
        admin.setEmail(email);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setPerfil("ADMIN");
        usuarioRepository.save(admin);
    }
}
