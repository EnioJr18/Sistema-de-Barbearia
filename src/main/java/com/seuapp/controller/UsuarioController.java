package com.seuapp.controller;

import com.seuapp.dto.UsuarioCreateRequestDTO;
import com.seuapp.dto.ErroApiDTO;
import com.seuapp.dto.UsuarioResponseDTO;
import com.seuapp.dto.UsuarioSenhaUpdateRequestDTO;
import com.seuapp.dto.UsuarioUpdateRequestDTO;
import com.seuapp.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuarios", description = "Cadastro publico, gestao administrativa e atualizacao de usuarios.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Lista usuarios", description = "Lista usuarios de forma paginada com filtro opcional por nome. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UsuarioResponseDTO> listarTodos(
            @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "Filtro opcional por nome") @RequestParam(required = false) String nome) {

        return usuarioService.listarTodos(pageable, nome);
    }

    @Operation(summary = "Cadastra cliente", description = "Cadastro publico. Qualquer perfil enviado no body e ignorado e o usuario e criado como CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PostMapping
    public UsuarioResponseDTO cadastrar(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return usuarioService.cadastrarCliente(request);
    }

    @Operation(summary = "Cadastra administrador", description = "Cria usuario com perfil ADMIN. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public UsuarioResponseDTO cadastrarAdmin(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return usuarioService.cadastrarAdmin(request);
    }

    @Operation(summary = "Cadastra barbeiro", description = "Cria usuario com perfil BARBEIRO. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/barbeiro")
    public UsuarioResponseDTO cadastrarBarbeiro(@RequestBody @Valid UsuarioCreateRequestDTO request) {
        return usuarioService.cadastrarBarbeiro(request);
    }

    @Operation(summary = "Busca usuario por ID", description = "ADMIN pode buscar qualquer usuario; usuarios autenticados podem buscar o proprio cadastro.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @Operation(summary = "Remove usuario", description = "Remove usuario por ID. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
    }

    @Operation(summary = "Atualiza usuario", description = "Atualiza nome, email e perfil quando permitido. Este endpoint nao altera senha.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequestDTO request) {
        return usuarioService.atualizar(id, request);
    }

    @Operation(summary = "Atualiza senha", description = "Altera a senha do usuario usando BCrypt. ADMIN pode alterar qualquer usuario; usuario autenticado pode alterar a propria senha.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or @controleAcessoService.isUsuarioAutenticado(#id)")
    @PutMapping("/{id}/senha")
    public UsuarioResponseDTO atualizarSenha(@PathVariable Long id, @RequestBody @Valid UsuarioSenhaUpdateRequestDTO request) {
        return usuarioService.atualizarSenha(id, request);
    }
}
