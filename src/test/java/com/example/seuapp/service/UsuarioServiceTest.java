package com.example.seuapp.service;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioSenhaUpdateRequestDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.mapper.UsuarioMapper;
import com.seuapp.model.Usuario;
import com.seuapp.repository.UsuarioRepository;
import com.seuapp.security.ControleAcessoService;
import com.seuapp.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ControleAcessoService controleAcessoService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, new UsuarioMapper(), controleAcessoService);
    }

    @Test
    void cadastroPublicoForcaPerfilCliente() {
        UsuarioCreateRequestDTO request = usuarioCreateRequest("ADMIN");
        when(passwordEncoder.encode("senha123")).thenReturn("hash-cliente");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponseDTO response = usuarioService.cadastrarCliente(request);

        assertEquals("CLIENTE", response.getPerfil());
        verify(usuarioRepository).save(argThat(usuario -> "CLIENTE".equals(usuario.getPerfil())));
    }

    @Test
    void criacaoDeBarbeiroDefinePerfilBarbeiro() {
        UsuarioCreateRequestDTO request = usuarioCreateRequest("CLIENTE");
        when(passwordEncoder.encode("senha123")).thenReturn("hash-barbeiro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(2L);
            return usuario;
        });

        UsuarioResponseDTO response = usuarioService.cadastrarBarbeiro(request);

        assertEquals("BARBEIRO", response.getPerfil());
        verify(usuarioRepository).save(argThat(usuario -> "BARBEIRO".equals(usuario.getPerfil())));
    }

    @Test
    void criacaoDeAdminDefinePerfilAdmin() {
        UsuarioCreateRequestDTO request = usuarioCreateRequest("CLIENTE");
        when(passwordEncoder.encode("senha123")).thenReturn("hash-admin");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(3L);
            return usuario;
        });

        UsuarioResponseDTO response = usuarioService.cadastrarAdmin(request);

        assertEquals("ADMIN", response.getPerfil());
        verify(usuarioRepository).save(argThat(usuario -> "ADMIN".equals(usuario.getPerfil())));
    }

    @Test
    void senhaEcriptografadaNoCadastro() {
        UsuarioCreateRequestDTO request = usuarioCreateRequest("CLIENTE");
        when(passwordEncoder.encode("senha123")).thenReturn("hash-gerado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.cadastrarCliente(request);

        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(argThat(usuario -> "hash-gerado".equals(usuario.getSenha())));
    }

    @Test
    void atualizarUsuarioNaoAlteraSenha() {
        Usuario usuario = usuario("hash-original", "CLIENTE");
        UsuarioUpdateRequestDTO request = new UsuarioUpdateRequestDTO();
        request.setNome("Novo Nome");
        request.setEmail("novo@example.com");
        request.setPerfil("ADMIN");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(controleAcessoService.isAdmin()).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO response = usuarioService.atualizar(1L, request);

        assertEquals("Novo Nome", response.getNome());
        assertEquals("hash-original", usuario.getSenha());
        assertEquals("CLIENTE", usuario.getPerfil());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void atualizarUsuarioPermiteAdminAlterarPerfil() {
        Usuario usuario = usuario("hash-original", "CLIENTE");
        UsuarioUpdateRequestDTO request = new UsuarioUpdateRequestDTO();
        request.setNome("Novo Nome");
        request.setEmail("novo@example.com");
        request.setPerfil("BARBEIRO");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(controleAcessoService.isAdmin()).thenReturn(true);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO response = usuarioService.atualizar(1L, request);

        assertEquals("BARBEIRO", response.getPerfil());
        assertEquals("hash-original", usuario.getSenha());
    }

    @Test
    void atualizarSenhaCriptografaNovaSenha() {
        Usuario usuario = usuario("hash-antigo", "CLIENTE");
        UsuarioSenhaUpdateRequestDTO request = new UsuarioSenhaUpdateRequestDTO();
        request.setSenha("novaSenha");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("hash-novo");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO response = usuarioService.atualizarSenha(1L, request);

        assertEquals("CLIENTE", response.getPerfil());
        assertEquals("hash-novo", usuario.getSenha());
        verify(passwordEncoder).encode("novaSenha");
    }

    private UsuarioCreateRequestDTO usuarioCreateRequest(String perfil) {
        UsuarioCreateRequestDTO request = new UsuarioCreateRequestDTO();
        request.setNome("Usuario");
        request.setEmail("usuario@example.com");
        request.setSenha("senha123");
        request.setPerfil(perfil);
        return request;
    }

    private Usuario usuario(String senha, String perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuario");
        usuario.setEmail("usuario@example.com");
        usuario.setSenha(senha);
        usuario.setPerfil(perfil);
        return usuario;
    }
}
