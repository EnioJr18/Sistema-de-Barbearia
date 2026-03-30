package com.seuapp.controller;

import com.seuapp.model.Servico;
import com.seuapp.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoRepository servicoRepository;

    @GetMapping
    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    @PostMapping
    public Servico cadastrar(@RequestBody Servico servico) {
        return servicoRepository.save(servico);
    }

    @GetMapping("/{id}")
    public Servico buscarPorId(@PathVariable Long id) {
        return servicoRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        servicoRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable Long id, @RequestBody Servico servicoAtualizado) {
        return servicoRepository.findById(id)
                .map(servico -> {
                    servico.setNome(servicoAtualizado.getNome());
                    servico.setDescricao(servicoAtualizado.getDescricao());
                    servico.setPreco(servicoAtualizado.getPreco());
                    servico.setDuracaoEmMinutos(servicoAtualizado.getDuracaoEmMinutos());
                    return servicoRepository.save(servico);
                })
                .orElse(null);
    }
}