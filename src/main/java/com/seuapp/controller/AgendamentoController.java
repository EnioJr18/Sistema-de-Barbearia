package com.seuapp.controller;

import com.seuapp.model.Agendamento;
import com.seuapp.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;

    @GetMapping
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    @PostMapping
    public Agendamento cadastrar(@RequestBody Agendamento agendamento) {
        return agendamentoRepository.save(agendamento);
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
