package com.seuapp.service;

import com.seuapp.dto.AgendamentoCreateRequestDTO;
import com.seuapp.dto.ReferenciaRequestDTO;
import com.seuapp.model.Agendamento;
import com.seuapp.model.Usuario;
import com.seuapp.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ControleAcessoService {

    private final AgendamentoRepository agendamentoRepository;

    public boolean isUsuarioAutenticado(Long usuarioId) {
        Usuario usuario = usuarioAtual();
        return usuario != null && usuario.getId() != null && usuario.getId().equals(usuarioId);
    }

    public boolean isAdmin() {
        return temPerfil("ADMIN");
    }

    public boolean podeCriarAgendamento(AgendamentoCreateRequestDTO request) {
        if (isAdmin()) {
            return true;
        }

        Usuario usuario = usuarioAtual();
        if (usuario == null || usuario.getId() == null) {
            return false;
        }

        if ("CLIENTE".equals(usuario.getPerfil())) {
            return usuario.getId().equals(resolveId(request.getClienteId(), request.getCliente()));
        }

        if ("BARBEIRO".equals(usuario.getPerfil())) {
            return usuario.getId().equals(resolveId(request.getBarbeiroId(), request.getBarbeiro()));
        }

        return false;
    }

    public boolean podeAcessarAgendamento(Long agendamentoId) {
        if (isAdmin()) {
            return true;
        }

        Usuario usuario = usuarioAtual();
        if (usuario == null || usuario.getId() == null) {
            return false;
        }

        return agendamentoRepository.findById(agendamentoId)
                .map(agendamento -> isClienteDoAgendamento(usuario, agendamento)
                        || isBarbeiroDoAgendamento(usuario, agendamento))
                .orElse(false);
    }

    public boolean podeAtualizarAgendamento(Long agendamentoId) {
        if (isAdmin()) {
            return true;
        }

        Usuario usuario = usuarioAtual();
        if (usuario == null || usuario.getId() == null || !"BARBEIRO".equals(usuario.getPerfil())) {
            return false;
        }

        return agendamentoRepository.findById(agendamentoId)
                .map(agendamento -> isBarbeiroDoAgendamento(usuario, agendamento))
                .orElse(false);
    }

    private boolean isClienteDoAgendamento(Usuario usuario, Agendamento agendamento) {
        return agendamento.getCliente() != null
                && agendamento.getCliente().getId() != null
                && agendamento.getCliente().getId().equals(usuario.getId());
    }

    private boolean isBarbeiroDoAgendamento(Usuario usuario, Agendamento agendamento) {
        return agendamento.getBarbeiro() != null
                && agendamento.getBarbeiro().getId() != null
                && agendamento.getBarbeiro().getId().equals(usuario.getId());
    }

    private boolean temPerfil(String perfil) {
        Usuario usuario = usuarioAtual();
        return usuario != null && perfil.equals(usuario.getPerfil());
    }

    private Usuario usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return null;
        }
        return usuario;
    }

    private Long resolveId(Long directId, ReferenciaRequestDTO reference) {
        if (directId != null) {
            return directId;
        }
        return reference != null ? reference.getId() : null;
    }
}
