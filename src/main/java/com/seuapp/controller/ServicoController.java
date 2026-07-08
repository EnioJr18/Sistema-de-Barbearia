package com.seuapp.controller;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.service.ServicoService;
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

    private final ServicoService servicoService;

    @GetMapping
    public Page<ServicoResponseDTO> listarTodos(@PageableDefault(size = 10) Pageable pageable,
                                                @RequestParam(required = false) String nome) {
        return servicoService.listarTodos(pageable, nome);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEIRO')")
    @PostMapping
    public ServicoResponseDTO cadastrar(@RequestBody @Valid ServicoCreateRequestDTO request) {
        return servicoService.cadastrar(request);
    }

    @GetMapping("/{id}")
    public ServicoResponseDTO buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        servicoService.deletar(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEIRO')")
    @PutMapping("/{id}")
    public ServicoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid ServicoUpdateRequestDTO request) {
        return servicoService.atualizar(id, request);
    }
}
