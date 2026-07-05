package com.seuapp.controller;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioSenhaUpdateRequestDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.mapper.UsuarioMapper;
import com.seuapp.model.Usuario;
import com.seuapp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

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

        return paginaDeUsuarios.map(usuarioMapper::toResponse);
    }

    @PostMapping
    public UsuarioResponseDTO cadastrar(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        Usuario usuario = usuarioMapper.toEntity(request);
        String senhaTriturada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaTriturada);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(usuarioSalvo);
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequestDTO request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioMapper.updateEntity(usuario, request);
                    Usuario usuarioSalvo = usuarioRepository.save(usuario);
                    return usuarioMapper.toResponse(usuarioSalvo);
                })
                .orElse(null);
    }

    @PutMapping("/{id}/senha")
    public UsuarioResponseDTO atualizarSenha(@PathVariable Long id, @RequestBody @Valid UsuarioSenhaUpdateRequestDTO request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    String senhaTriturada = passwordEncoder.encode(request.getSenha());
                    usuario.setSenha(senhaTriturada);
                    Usuario usuarioSalvo = usuarioRepository.save(usuario);
                    return usuarioMapper.toResponse(usuarioSalvo);
                })
                .orElse(null);
    }
}
