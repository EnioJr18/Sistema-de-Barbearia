package com.seuapp.controller;

import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.model.Usuario;
import com.seuapp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


    @GetMapping
    public Page<UsuarioResponseDTO> listarTodos(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String nome) {

        Page<Usuario> paginaDeUsuarios;

        if (nome != null) {
            paginaDeUsuarios = usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            paginaDeUsuarios = usuarioRepository.findAll(pageable);
        }

        return paginaDeUsuarios.map(usuario -> {
            UsuarioResponseDTO dto = new UsuarioResponseDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            dto.setPerfil(usuario.getPerfil());
            return dto;
        });
    }

    @PostMapping
    public UsuarioResponseDTO cadastrar(@RequestBody Usuario usuario) {
        String senhaTriturada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaTriturada);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuarioSalvo.getId());
        dto.setNome(usuarioSalvo.getNome());
        dto.setEmail(usuarioSalvo.getEmail());
        dto.setPerfil(usuarioSalvo.getPerfil());

        return dto;
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    UsuarioResponseDTO dto = new UsuarioResponseDTO();
                    dto.setId(usuario.getId());
                    dto.setNome(usuario.getNome());
                    dto.setEmail(usuario.getEmail());
                    dto.setPerfil(usuario.getPerfil());
                    return dto;
                })
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNome(usuarioAtualizado.getNome());
                    usuario.setEmail(usuarioAtualizado.getEmail());
                    usuario.setSenha(usuarioAtualizado.getSenha());
                    usuario.setPerfil(usuarioAtualizado.getPerfil());
                    return usuarioRepository.save(usuario);
                })
                .orElse(null);
    }
}