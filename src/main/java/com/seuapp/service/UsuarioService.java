package com.seuapp.service;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioSenhaUpdateRequestDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.mapper.UsuarioMapper;
import com.seuapp.model.Usuario;
import com.seuapp.repository.UsuarioRepository;
import com.seuapp.security.ControleAcessoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String PERFIL_ADMIN = "ADMIN";
    private static final String PERFIL_BARBEIRO = "BARBEIRO";
    private static final String PERFIL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final ControleAcessoService controleAcessoService;

    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable, String nome) {
        Page<Usuario> paginaDeUsuarios;

        if (nome != null) {
            paginaDeUsuarios = usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            paginaDeUsuarios = usuarioRepository.findAll(pageable);
        }

        return paginaDeUsuarios.map(usuarioMapper::toResponse);
    }

    public UsuarioResponseDTO cadastrarCliente(UsuarioCreateRequestDTO request) {
        return criarUsuario(request, PERFIL_CLIENTE);
    }

    public UsuarioResponseDTO cadastrarAdmin(UsuarioCreateRequestDTO request) {
        return criarUsuario(request, PERFIL_ADMIN);
    }

    public UsuarioResponseDTO cadastrarBarbeiro(UsuarioCreateRequestDTO request) {
        return criarUsuario(request, PERFIL_BARBEIRO);
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateRequestDTO request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioMapper.updateEntity(usuario, request);
                    if (controleAcessoService.isAdmin() && hasText(request.getPerfil())) {
                        usuario.setPerfil(request.getPerfil());
                    }
                    Usuario usuarioSalvo = usuarioRepository.save(usuario);
                    return usuarioMapper.toResponse(usuarioSalvo);
                })
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));
    }

    public UsuarioResponseDTO atualizarSenha(Long id, UsuarioSenhaUpdateRequestDTO request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    String senhaTriturada = passwordEncoder.encode(request.getSenha());
                    usuario.setSenha(senhaTriturada);
                    Usuario usuarioSalvo = usuarioRepository.save(usuario);
                    return usuarioMapper.toResponse(usuarioSalvo);
                })
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));
    }

    public void deletar(Long id) {
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO criarUsuario(UsuarioCreateRequestDTO request, String perfil) {
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setPerfil(perfil);
        String senhaTriturada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaTriturada);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(usuarioSalvo);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
