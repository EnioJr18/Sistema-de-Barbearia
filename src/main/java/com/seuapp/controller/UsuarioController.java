package com.seuapp.controller;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioSenhaUpdateRequestDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.mapper.UsuarioMapper;
import com.seuapp.model.Usuario;
import com.seuapp.repository.UsuarioRepository;
import com.seuapp.service.ControleAcessoService;
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
    private final ControleAcessoService controleAcessoService;

    @PreAuthorize("hasRole('ADMIN')")
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
        return criarUsuario(request, "CLIENTE");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public UsuarioResponseDTO cadastrarAdmin(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return criarUsuario(request, "ADMIN");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/barbeiro")
    public UsuarioResponseDTO cadastrarBarbeiro(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return criarUsuario(request, "BARBEIRO");
    }

    private UsuarioResponseDTO criarUsuario(UsuarioCreateRequestDTO request, String perfil) {
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setPerfil(perfil);
        String senhaTriturada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaTriturada);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(usuarioSalvo);
    }

    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
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

    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequestDTO request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioMapper.updateEntity(usuario, request);
                    if (controleAcessoService.isAdmin() && hasText(request.getPerfil())) {
                        usuario.setPerfil(request.getPerfil());
                    }
                    Usuario usuarioSalvo = usuarioRepository.save(usuario);
                    return usuarioMapper.toResponse(usuarioSalvo);
                })
                .orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
