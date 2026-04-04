package com.seuapp.controller;

import com.seuapp.dto.AgendamentoResponseDTO;
import com.seuapp.model.Agendamento;
import com.seuapp.repository.AgendamentoRepository;
import com.seuapp.service.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final  AgendamentoService agendamentoService;

    @GetMapping
    public Page<AgendamentoResponseDTO> listar(@RequestParam(required = false) Long barbeiroId,
                                               @RequestParam(required = false) String nomeCliente,
                                               @PageableDefault(size = 10) Pageable pageable) {

        Page<Agendamento> paginaDeAgendamentos;
        if (barbeiroId != null) {
            paginaDeAgendamentos = agendamentoRepository.findByBarbeiroId(barbeiroId, pageable);
        }
        else if (nomeCliente != null) {
        paginaDeAgendamentos = agendamentoRepository.findByCliente_NomeContainingIgnoreCase(nomeCliente, pageable);

        } else {
            paginaDeAgendamentos = agendamentoRepository.findAll(pageable);
        }
        return paginaDeAgendamentos.map(agendamento -> {
            AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
            dto.setNomeCliente(agendamento.getCliente().getNome());
            dto.setNomeServico(agendamento.getServico().getNome());
            return dto;
        });
    }

    @PostMapping
    public Agendamento cadastrar(@RequestBody Agendamento agendamento) {
        return agendamentoService.agendar(agendamento);
    }

    @GetMapping("/{id}")
    public Agendamento buscarPorId(@PathVariable Long id) {
        return agendamentoRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        agendamentoRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Agendamento atualizar(@PathVariable Long id, @RequestBody Agendamento agendamentoAtualizado) {
        return agendamentoRepository.findById(id)
                .map(agendamento -> {
                    agendamento.setDataEHora(agendamentoAtualizado.getDataEHora());
                    agendamento.setStatus(agendamentoAtualizado.getStatus());
                    agendamento.setFormaDePagamento(agendamentoAtualizado.getFormaDePagamento());
                    agendamento.setCliente(agendamentoAtualizado.getCliente());
                    agendamento.setBarbeiro(agendamentoAtualizado.getBarbeiro());
                    agendamento.setServico(agendamentoAtualizado.getServico());

                    return agendamentoRepository.save(agendamento);
                })
                .orElse(null);
    }
}
