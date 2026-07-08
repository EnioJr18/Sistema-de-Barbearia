package com.seuapp.mapper;

import com.seuapp.dto.AgendamentoCreateRequestDTO;
import com.seuapp.dto.AgendamentoResponseDTO;
import com.seuapp.dto.AgendamentoUpdateRequestDTO;
import com.seuapp.dto.ReferenciaRequestDTO;
import com.seuapp.dto.ServicoResumoResponseDTO;
import com.seuapp.dto.UsuarioResumoResponseDTO;
import com.seuapp.mapper.AgendamentoMapper;
import com.seuapp.mapper.ServicoMapper;
import com.seuapp.mapper.UsuarioMapper;
import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.FormaPagamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import com.seuapp.model.Servico;
import com.seuapp.model.Usuario;
import com.seuapp.repository.ServicoRepository;
import com.seuapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoMapperTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private AgendamentoMapper agendamentoMapper;
    private Usuario cliente;
    private Usuario barbeiro;
    private Servico servico;

    @BeforeEach
    void setUp() {
        agendamentoMapper = new AgendamentoMapper(
                new UsuarioMapper(),
                new ServicoMapper(),
                usuarioRepository,
                servicoRepository);

        cliente = usuario(1L, "Cliente", "cliente@example.com", "CLIENTE", "hash-cliente");
        barbeiro = usuario(2L, "Barbeiro", "barbeiro@example.com", "BARBEIRO", "hash-barbeiro");
        servico = servico(3L, "Corte");
    }

    @Test
    void toEntityComIdsDiretosConverteParaEntidade() {
        AgendamentoCreateRequestDTO request = createRequestComIdsDiretos();
        mockFinds();

        Agendamento agendamento = agendamentoMapper.toEntity(request);

        assertEquals(cliente, agendamento.getCliente());
        assertEquals(barbeiro, agendamento.getBarbeiro());
        assertEquals(servico, agendamento.getServico());
        assertEquals(request.getDataEHora(), agendamento.getDataEHora());
        assertEquals(FormaPagamento.PIX, agendamento.getFormaDePagamento());
    }

    @Test
    void toEntityComReferenciasAninhadasConverteParaEntidade() {
        AgendamentoCreateRequestDTO request = createRequestComReferencias();
        mockFinds();

        Agendamento agendamento = agendamentoMapper.toEntity(request);

        assertEquals(cliente.getId(), agendamento.getCliente().getId());
        assertEquals(barbeiro.getId(), agendamento.getBarbeiro().getId());
        assertEquals(servico.getId(), agendamento.getServico().getId());
    }

    @Test
    void toEntityDefineStatusPendenteNaCriacao() {
        AgendamentoCreateRequestDTO request = createRequestComIdsDiretos();
        mockFinds();

        Agendamento agendamento = agendamentoMapper.toEntity(request);

        assertEquals(StatusAgendamento.PENDENTE, agendamento.getStatus());
    }

    @Test
    void toResponseConverteAgendamentoParaResponse() {
        Agendamento agendamento = agendamento(StatusAgendamento.CONFIRMADO, FormaPagamento.CARTAO_CREDITO);

        AgendamentoResponseDTO response = agendamentoMapper.toResponse(agendamento);

        assertEquals(10L, response.getId());
        assertEquals("Cliente", response.getCliente().getNome());
        assertEquals("Barbeiro", response.getBarbeiro().getNome());
        assertEquals("Corte", response.getServico().getNome());
    }

    @Test
    void responseRetornaClienteResumidoSemSenha() {
        AgendamentoResponseDTO response = agendamentoMapper.toResponse(agendamento(StatusAgendamento.PENDENTE, FormaPagamento.PIX));

        assertFalse(possuiCampoSenha(response.getCliente()));
        assertInstanceOf(UsuarioResumoResponseDTO.class, response.getCliente());
    }

    @Test
    void responseRetornaBarbeiroResumidoSemSenha() {
        AgendamentoResponseDTO response = agendamentoMapper.toResponse(agendamento(StatusAgendamento.PENDENTE, FormaPagamento.PIX));

        assertFalse(possuiCampoSenha(response.getBarbeiro()));
        assertInstanceOf(UsuarioResumoResponseDTO.class, response.getBarbeiro());
    }

    @Test
    void responseRetornaServicoResumido() {
        AgendamentoResponseDTO response = agendamentoMapper.toResponse(agendamento(StatusAgendamento.PENDENTE, FormaPagamento.PIX));

        assertInstanceOf(ServicoResumoResponseDTO.class, response.getServico());
        assertEquals(3L, response.getServico().getId());
        assertEquals("Corte", response.getServico().getNome());
        assertEquals(BigDecimal.valueOf(35), response.getServico().getPreco());
    }

    @Test
    void responsePreservaDataFormaPagamentoEStatus() {
        Agendamento agendamento = agendamento(StatusAgendamento.CONCLUIDO, FormaPagamento.DINHEIRO);

        AgendamentoResponseDTO response = agendamentoMapper.toResponse(agendamento);

        assertEquals(agendamento.getDataEHora(), response.getDataEHora());
        assertEquals(StatusAgendamento.CONCLUIDO, response.getStatus());
        assertEquals(FormaPagamento.DINHEIRO, response.getFormaDePagamento());
    }

    @Test
    void updateEntityAtualizaCamposDoAgendamento() {
        Agendamento agendamento = agendamento(StatusAgendamento.PENDENTE, FormaPagamento.PIX);
        AgendamentoUpdateRequestDTO request = updateRequest();
        mockFinds();

        agendamentoMapper.updateEntity(agendamento, request);

        assertEquals(request.getDataEHora(), agendamento.getDataEHora());
        assertEquals(StatusAgendamento.CONFIRMADO, agendamento.getStatus());
        assertEquals(FormaPagamento.CARTAO_DEBITO, agendamento.getFormaDePagamento());
        assertEquals(cliente, agendamento.getCliente());
        assertEquals(barbeiro, agendamento.getBarbeiro());
        assertEquals(servico, agendamento.getServico());
    }

    private void mockFinds() {
        when(usuarioRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(barbeiro.getId())).thenReturn(Optional.of(barbeiro));
        when(servicoRepository.findById(servico.getId())).thenReturn(Optional.of(servico));
    }

    private AgendamentoCreateRequestDTO createRequestComIdsDiretos() {
        AgendamentoCreateRequestDTO request = new AgendamentoCreateRequestDTO();
        request.setClienteId(cliente.getId());
        request.setBarbeiroId(barbeiro.getId());
        request.setServicoId(servico.getId());
        request.setDataEHora(LocalDateTime.of(2026, 7, 10, 9, 0));
        request.setFormaDePagamento(FormaPagamento.PIX);
        return request;
    }

    private AgendamentoCreateRequestDTO createRequestComReferencias() {
        AgendamentoCreateRequestDTO request = new AgendamentoCreateRequestDTO();
        request.setCliente(referencia(cliente.getId()));
        request.setBarbeiro(referencia(barbeiro.getId()));
        request.setServico(referencia(servico.getId()));
        request.setDataEHora(LocalDateTime.of(2026, 7, 10, 9, 0));
        request.setFormaDePagamento(FormaPagamento.PIX);
        return request;
    }

    private AgendamentoUpdateRequestDTO updateRequest() {
        AgendamentoUpdateRequestDTO request = new AgendamentoUpdateRequestDTO();
        request.setClienteId(cliente.getId());
        request.setBarbeiroId(barbeiro.getId());
        request.setServicoId(servico.getId());
        request.setDataEHora(LocalDateTime.of(2026, 7, 10, 10, 0));
        request.setStatus(StatusAgendamento.CONFIRMADO);
        request.setFormaDePagamento(FormaPagamento.CARTAO_DEBITO);
        return request;
    }

    private ReferenciaRequestDTO referencia(Long id) {
        ReferenciaRequestDTO referencia = new ReferenciaRequestDTO();
        referencia.setId(id);
        return referencia;
    }

    private Agendamento agendamento(StatusAgendamento status, FormaPagamento formaPagamento) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(10L);
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataEHora(LocalDateTime.of(2026, 7, 10, 9, 0));
        agendamento.setStatus(status);
        agendamento.setFormaDePagamento(formaPagamento);
        return agendamento;
    }

    private Usuario usuario(Long id, String nome, String email, String perfil, String senha) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setSenha(senha);
        return usuario;
    }

    private Servico servico(Long id, String nome) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setNome(nome);
        servico.setDescricao(nome + " descricao");
        servico.setPreco(BigDecimal.valueOf(35));
        servico.setDuracaoEmMinutos(40);
        return servico;
    }

    private boolean possuiCampoSenha(Object value) {
        return Arrays.stream(value.getClass().getDeclaredFields())
                .map(Field::getName)
                .anyMatch("senha"::equals);
    }
}
