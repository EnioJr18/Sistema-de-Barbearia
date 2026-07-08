package com.seuapp.controller;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioSenhaUpdateRequestDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UsuarioResponseDTO> listarTodos(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String nome) {

        return usuarioService.listarTodos(pageable, nome);
    }

    @PostMapping
    public UsuarioResponseDTO cadastrar(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return usuarioService.cadastrarCliente(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public UsuarioResponseDTO cadastrarAdmin(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return usuarioService.cadastrarAdmin(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/barbeiro")
    public UsuarioResponseDTO cadastrarBarbeiro(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return usuarioService.cadastrarBarbeiro(request);
    }

    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequestDTO request) {
        return usuarioService.atualizar(id, request);
    }

    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @PutMapping("/{id}/senha")
    public UsuarioResponseDTO atualizarSenha(@PathVariable Long id, @RequestBody @Valid UsuarioSenhaUpdateRequestDTO request) {
        return usuarioService.atualizarSenha(id, request);
    }
}
