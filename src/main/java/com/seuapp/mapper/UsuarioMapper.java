package com.seuapp.mapper;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioResumoResponseDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioCreateRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(request.getSenha());
        usuario.setPerfil(request.getPerfil());
        return usuario;
    }

    public void updateEntity(Usuario usuario, UsuarioUpdateRequestDTO request) {
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setPerfil(usuario.getPerfil());
        return response;
    }

    public UsuarioResumoResponseDTO toResumoResponse(Usuario usuario) {
        UsuarioResumoResponseDTO response = new UsuarioResumoResponseDTO();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        return response;
    }
}
