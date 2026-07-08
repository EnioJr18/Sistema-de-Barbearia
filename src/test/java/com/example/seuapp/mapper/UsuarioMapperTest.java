package com.example.seuapp.mapper;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioResumoResponseDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.mapper.UsuarioMapper;
import com.seuapp.model.Usuario;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private final UsuarioMapper usuarioMapper = new UsuarioMapper();

    @Test
    void deveConverterCreateRequestParaEntidade() {
        UsuarioCreateRequestDTO request = new UsuarioCreateRequestDTO();
        request.setNome("Cliente");
        request.setEmail("cliente@example.com");
        request.setSenha("senha123");
        request.setPerfil("CLIENTE");

        Usuario usuario = usuarioMapper.toEntity(request);

        assertEquals("Cliente", usuario.getNome());
        assertEquals("cliente@example.com", usuario.getEmail());
        assertEquals("senha123", usuario.getSenha());
        assertEquals("CLIENTE", usuario.getPerfil());
    }

    @Test
    void usuarioResponseNuncaContemSenhaOuHash() {
        Usuario usuario = usuario("hash-bcrypt");

        UsuarioResponseDTO response = usuarioMapper.toResponse(usuario);

        assertEquals(1L, response.getId());
        assertEquals("Cliente", response.getNome());
        assertEquals("cliente@example.com", response.getEmail());
        assertEquals("CLIENTE", response.getPerfil());
        assertNaoPossuiCampo(response.getClass(), "senha");
    }

    @Test
    void usuarioResumoResponseNuncaContemSenhaOuHash() {
        Usuario usuario = usuario("hash-bcrypt");

        UsuarioResumoResponseDTO response = usuarioMapper.toResumoResponse(usuario);

        assertEquals(1L, response.getId());
        assertEquals("Cliente", response.getNome());
        assertNaoPossuiCampo(response.getClass(), "senha");
        assertNaoPossuiCampo(response.getClass(), "email");
        assertNaoPossuiCampo(response.getClass(), "perfil");
    }

    @Test
    void updateEntityNaoAlteraSenhaNemPerfil() {
        Usuario usuario = usuario("hash-original");
        usuario.setPerfil("CLIENTE");

        UsuarioUpdateRequestDTO request = new UsuarioUpdateRequestDTO();
        request.setNome("Novo Nome");
        request.setEmail("novo@example.com");
        request.setPerfil("ADMIN");

        usuarioMapper.updateEntity(usuario, request);

        assertEquals("Novo Nome", usuario.getNome());
        assertEquals("novo@example.com", usuario.getEmail());
        assertEquals("hash-original", usuario.getSenha());
        assertEquals("CLIENTE", usuario.getPerfil());
    }

    private Usuario usuario(String senha) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Cliente");
        usuario.setEmail("cliente@example.com");
        usuario.setSenha(senha);
        usuario.setPerfil("CLIENTE");
        return usuario;
    }

    private void assertNaoPossuiCampo(Class<?> clazz, String nomeCampo) {
        for (Field field : clazz.getDeclaredFields()) {
            assertNotEquals(nomeCampo, field.getName());
        }
    }
}
