package com.seuapp.controller;

import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.model.Servico;
import com.seuapp.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoRepository servicoRepository;

    @GetMapping
    public Page<ServicoResponseDTO> listarTodos(@PageableDefault(size = 10) Pageable pageable,
                                                @RequestParam(required = false) String nome) {
        Page<Servico> paginaDeServicos;

        if (nome != null) {
            paginaDeServicos = servicoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        }
        else {
            paginaDeServicos = servicoRepository.findAll(pageable);
        }
        return paginaDeServicos.map(servico -> {
            ServicoResponseDTO dto = new ServicoResponseDTO();
            dto.setId(servico.getId());
            dto.setNome(servico.getNome());
            dto.setDescricao(servico.getDescricao());
            dto.setPreco(servico.getPreco());
            dto.setDuracaoEmMinutos(servico.getDuracaoEmMinutos());
            return dto;
        });
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