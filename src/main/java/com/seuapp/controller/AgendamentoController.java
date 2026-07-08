package com.seuapp.controller;

import com.seuapp.dto.AgendamentoCreateRequestDTO;
import com.seuapp.dto.AgendamentoResponseDTO;
import com.seuapp.dto.AgendamentoUpdateRequestDTO;
import com.seuapp.dto.ErroApiDTO;
import com.seuapp.mapper.AgendamentoMapper;
import com.seuapp.model.Agendamento;
import com.seuapp.repository.AgendamentoRepository;
import com.seuapp.service.AgendamentoService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Criacao, consulta, atualizacao, cancelamento e horarios disponiveis.")
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;
    private final AgendamentoMapper agendamentoMapper;

    @Operation(summary = "Lista agendamentos", description = "Lista agendamentos de forma paginada com filtros opcionais por barbeiro ou nome do cliente. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<AgendamentoResponseDTO> listar(@Parameter(description = "Filtro opcional por ID do barbeiro") @RequestParam(required = false) Long barbeiroId,
                                               @Parameter(description = "Filtro opcional por nome do cliente") @RequestParam(required = false) String nomeCliente,
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

    @Operation(summary = "Lista horarios disponiveis", description = "Gera horarios entre 08:00 e 18:00 em blocos pela duracao do servico, remove conflitos por intervalo e ignora agendamentos CANCELADOS. Domingo retorna erro 400.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horarios disponiveis retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Data ou regra de horario invalida",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "404", description = "Barbeiro ou servico nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @GetMapping("/horarios-disponiveis")
    public List<String> listarHorariosDisponiveis(
            @Parameter(description = "ID do barbeiro") @RequestParam Long barbeiroId,
            @Parameter(description = "ID do servico") @RequestParam Long servicoId,
            @Parameter(description = "Data no formato yyyy-MM-dd") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        return agendamentoService.listarHorariosDisponiveis(barbeiroId, servicoId, data);
    }

    @Operation(summary = "Cria agendamento", description = "Cria agendamento com status inicial PENDENTE. Valida passado, domingo, horario 08:00-18:00 e conflito por intervalo considerando a duracao do servico.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou regra de horario violada",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de agenda",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PreAuthorize("@controleAcessoService.podeCriarAgendamento(#request)")
    @PostMapping
    public AgendamentoResponseDTO cadastrar(@RequestBody @Valid AgendamentoCreateRequestDTO request) {
        Agendamento agendamento = agendamentoMapper.toEntity(request);
        return agendamentoMapper.toResponse(agendamentoService.agendar(agendamento));
    }

    @Operation(summary = "Busca agendamento por ID", description = "ADMIN, cliente dono ou barbeiro responsavel podem consultar o agendamento.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@controleAcessoService.podeAcessarAgendamento(#id)")
    @GetMapping("/{id}")
    public AgendamentoResponseDTO buscarPorId(@PathVariable Long id) {
        return agendamentoMapper.toResponse(agendamentoService.buscarPorId(id));
    }

    @Operation(summary = "Remove agendamento", description = "Remove agendamento por ID. Exige perfil ADMIN. O fluxo recomendado para usuarios e cancelamento.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
    }

    @Operation(summary = "Atualiza agendamento", description = "Atualiza dados do agendamento. ADMIN ou barbeiro responsavel, conforme regra atual.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@controleAcessoService.podeAtualizarAgendamento(#id)")
    @PutMapping("/{id}")
    public AgendamentoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid AgendamentoUpdateRequestDTO request) {
        Agendamento agendamento = agendamentoService.buscarPorId(id);
        agendamentoMapper.updateEntity(agendamento, request);
        return agendamentoMapper.toResponse(agendamentoService.atualizar(id, agendamento));
    }

    @Operation(summary = "Cancela agendamento", description = "Altera o status para CANCELADO sem apagar o registro. Nao permite cancelar agendamento CANCELADO ou CONCLUIDO.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento cancelado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Agendamento ja cancelado ou concluido",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PreAuthorize("@controleAcessoService.podeCancelarAgendamento(#id)")
    @PatchMapping("/{id}/cancelar")
    public AgendamentoResponseDTO cancelar(@PathVariable Long id) {
        return agendamentoMapper.toResponse(agendamentoService.cancelar(id));
    }
}
