package com.seuapp.controller;

import com.seuapp.dto.AgendamentoCreateRequestDTO;
import com.seuapp.dto.AgendamentoResponseDTO;
import com.seuapp.dto.AgendamentoUpdateRequestDTO;
import com.seuapp.mapper.AgendamentoMapper;
import com.seuapp.model.Agendamento;
import com.seuapp.repository.AgendamentoRepository;
import com.seuapp.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;
    private final AgendamentoMapper agendamentoMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<AgendamentoResponseDTO> listar(@RequestParam(required = false) Long barbeiroId,
                                               @RequestParam(required = false) String nomeCliente,
                                               @PageableDefault(size = 10) Pageable pageable) {

        Page<Agendamento> paginaDeAgendamentos;
        if (barbeiroId != null) {
            paginaDeAgendamentos = agendamentoRepository.findByBarbeiroId(barbeiroId, pageable);
        } else if (nomeCliente != null) {
            paginaDeAgendamentos = agendamentoRepository.findByCliente_NomeContainingIgnoreCase(nomeCliente, pageable);
        } else {
            paginaDeAgendamentos = agendamentoRepository.findAll(pageable);
        }

        return paginaDeAgendamentos.map(agendamentoMapper::toResponse);
    }

    @PreAuthorize("@controleAcessoService.podeCriarAgendamento(#request)")
    @PostMapping
    public AgendamentoResponseDTO cadastrar(@RequestBody @Valid AgendamentoCreateRequestDTO request) {
        Agendamento agendamento = agendamentoMapper.toEntity(request);
        return agendamentoMapper.toResponse(agendamentoService.agendar(agendamento));
    }

    @PreAuthorize("@controleAcessoService.podeAcessarAgendamento(#id)")
    @GetMapping("/{id}")
    public AgendamentoResponseDTO buscarPorId(@PathVariable Long id) {
        return agendamentoRepository.findById(id)
                .map(agendamentoMapper::toResponse)
                .orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        agendamentoRepository.deleteById(id);
    }

    @PreAuthorize("@controleAcessoService.podeAtualizarAgendamento(#id)")
    @PutMapping("/{id}")
    public AgendamentoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid AgendamentoUpdateRequestDTO request) {
        return agendamentoRepository.findById(id)
                .map(agendamento -> {
                    agendamentoMapper.updateEntity(agendamento, request);
                    return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
                })
                .orElse(null);
    }

    @PreAuthorize("@controleAcessoService.podeCancelarAgendamento(#id)")
    @PatchMapping("/{id}/cancelar")
    public AgendamentoResponseDTO cancelar(@PathVariable Long id) {
        return agendamentoMapper.toResponse(agendamentoService.cancelar(id));
    }
}
