package com.seuapp.controller;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ErroApiDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.service.ServicoService;
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
@RequestMapping("/servicos")
@RequiredArgsConstructor
@Tag(name = "Servicos", description = "Consulta e gestao dos servicos oferecidos pela barbearia.")
public class ServicoController {

    private final ServicoService servicoService;

    @Operation(summary = "Lista servicos", description = "Lista servicos de forma paginada com filtro opcional por nome.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public Page<ServicoResponseDTO> listarTodos(@PageableDefault(size = 10) Pageable pageable,
                                                @Parameter(description = "Filtro opcional por nome") @RequestParam(required = false) String nome) {
        return servicoService.listarTodos(pageable, nome);
    }

    @Operation(summary = "Cadastra servico", description = "Cria um novo servico. Exige perfil ADMIN ou BARBEIRO.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servico cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEIRO')")
    @PostMapping
    public ServicoResponseDTO cadastrar(@RequestBody @Valid ServicoCreateRequestDTO request) {
        return servicoService.cadastrar(request);
    }

    @Operation(summary = "Busca servico por ID", description = "Busca um servico pelo identificador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servico encontrado"),
            @ApiResponse(responseCode = "404", description = "Servico nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @GetMapping("/{id}")
    public ServicoResponseDTO buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id);
    }

    @Operation(summary = "Remove servico", description = "Remove um servico. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        servicoService.deletar(id);
    }

    @Operation(summary = "Atualiza servico", description = "Atualiza dados de um servico. Exige perfil ADMIN ou BARBEIRO.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEIRO')")
    @PutMapping("/{id}")
    public ServicoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid ServicoUpdateRequestDTO request) {
        return servicoService.atualizar(id, request);
    }
}
