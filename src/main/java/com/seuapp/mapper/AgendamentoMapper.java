package com.seuapp.mapper;

import com.seuapp.dto.AgendamentoCreateRequestDTO;
import com.seuapp.dto.AgendamentoResponseDTO;
import com.seuapp.dto.AgendamentoUpdateRequestDTO;
import com.seuapp.dto.ReferenciaRequestDTO;
import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import com.seuapp.repository.ServicoRepository;
import com.seuapp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoMapper {

    private final UsuarioMapper usuarioMapper;
    private final ServicoMapper servicoMapper;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public AgendamentoMapper(
            UsuarioMapper usuarioMapper,
            ServicoMapper servicoMapper,
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository) {
        this.usuarioMapper = usuarioMapper;
        this.servicoMapper = servicoMapper;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
    }

    public Agendamento toEntity(AgendamentoCreateRequestDTO request) {
        Agendamento agendamento = new Agendamento();
        agendamento.setDataEHora(request.getDataEHora());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setFormaDePagamento(request.getFormaDePagamento());
        agendamento.setCliente(usuarioRepository.findById(resolveId(request.getClienteId(), request.getCliente()))
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado")));
        agendamento.setBarbeiro(usuarioRepository.findById(resolveId(request.getBarbeiroId(), request.getBarbeiro()))
                .orElseThrow(() -> new EntityNotFoundException("Barbeiro nao encontrado")));
        agendamento.setServico(servicoRepository.findById(resolveId(request.getServicoId(), request.getServico()))
                .orElseThrow(() -> new EntityNotFoundException("Servico nao encontrado")));
        return agendamento;
    }

    public void updateEntity(Agendamento agendamento, AgendamentoUpdateRequestDTO request) {
        agendamento.setDataEHora(request.getDataEHora());
        agendamento.setStatus(request.getStatus());
        agendamento.setFormaDePagamento(request.getFormaDePagamento());
        agendamento.setCliente(usuarioRepository.findById(resolveId(request.getClienteId(), request.getCliente()))
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado")));
        agendamento.setBarbeiro(usuarioRepository.findById(resolveId(request.getBarbeiroId(), request.getBarbeiro()))
                .orElseThrow(() -> new EntityNotFoundException("Barbeiro nao encontrado")));
        agendamento.setServico(servicoRepository.findById(resolveId(request.getServicoId(), request.getServico()))
                .orElseThrow(() -> new EntityNotFoundException("Servico nao encontrado")));
    }

    public AgendamentoResponseDTO toResponse(Agendamento agendamento) {
        AgendamentoResponseDTO response = new AgendamentoResponseDTO();
        response.setId(agendamento.getId());
        response.setDataEHora(agendamento.getDataEHora());
        response.setStatus(agendamento.getStatus());
        response.setFormaDePagamento(agendamento.getFormaDePagamento());
        response.setCliente(usuarioMapper.toResumoResponse(agendamento.getCliente()));
        response.setBarbeiro(usuarioMapper.toResumoResponse(agendamento.getBarbeiro()));
        response.setServico(servicoMapper.toResumoResponse(agendamento.getServico()));
        return response;
    }

    private Long resolveId(Long directId, ReferenciaRequestDTO reference) {
        if (directId != null) {
            return directId;
        }
        return reference != null ? reference.getId() : null;
    }
}
