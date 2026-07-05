package com.seuapp.controller;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.mapper.ServicoMapper;
import com.seuapp.model.Servico;
import com.seuapp.repository.ServicoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    @GetMapping
    public Page<ServicoResponseDTO> listarTodos(@PageableDefault(size = 10) Pageable pageable,
                                                @RequestParam(required = false) String nome) {
        Page<Servico> paginaDeServicos;

        if (nome != null) {
            paginaDeServicos = servicoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            paginaDeServicos = servicoRepository.findAll(pageable);
        }

        return paginaDeServicos.map(servicoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEIRO')")
    @PostMapping
    public ServicoResponseDTO cadastrar(@RequestBody @Valid ServicoCreateRequestDTO request) {
        Servico servico = servicoMapper.toEntity(request);
        return servicoMapper.toResponse(servicoRepository.save(servico));
    }

    @GetMapping("/{id}")
    public ServicoResponseDTO buscarPorId(@PathVariable Long id) {
        return servicoRepository.findById(id)
                .map(servicoMapper::toResponse)
                .orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        servicoRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEIRO')")
    @PutMapping("/{id}")
    public ServicoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid ServicoUpdateRequestDTO request) {
        return servicoRepository.findById(id)
                .map(servico -> {
                    servicoMapper.updateEntity(servico, request);
                    return servicoMapper.toResponse(servicoRepository.save(servico));
                })
                .orElse(null);
    }
}
