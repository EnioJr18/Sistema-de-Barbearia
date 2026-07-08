package com.seuapp.service;

import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.FormaPagamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import com.seuapp.model.Servico;
import com.seuapp.model.Usuario;
import com.seuapp.repository.AgendamentoRepository;
import com.seuapp.repository.ServicoRepository;
import com.seuapp.repository.UsuarioRepository;
import com.seuapp.service.AgendamentoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Test
    void deveCriarAgendamentoValido() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(9, 0), 40, StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of());
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.agendar(agendamento);

        assertSame(agendamento, resultado);
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void deveDefinirStatusPendenteQuandoCriarAgendamentoSemStatus() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(9, 0), 40, null);
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of());
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.agendar(agendamento);

        assertEquals(StatusAgendamento.PENDENTE, resultado.getStatus());
    }

    @Test
    void deveBloquearAgendamentoNoPassado() {
        Agendamento agendamento = agendamentoEm(LocalDateTime.now().minusDays(1), 40, StatusAgendamento.PENDENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.agendar(agendamento));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Nao e possivel realizar agendamentos no passado.", exception.getReason());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void deveBloquearAgendamentoAosDomingos() {
        Agendamento agendamento = agendamentoEm(proximoDomingo().atTime(9, 0), 40, StatusAgendamento.PENDENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.agendar(agendamento));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("A barbearia nao funciona aos domingos.", exception.getReason());
    }

    @Test
    void deveBloquearAgendamentoAntesDasOito() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(7, 59), 40, StatusAgendamento.PENDENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.agendar(agendamento));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("O horario de funcionamento inicia as 08:00.", exception.getReason());
    }

    @Test
    void deveBloquearAgendamentoQueTerminaDepoisDasDezoito() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(17, 30), 40, StatusAgendamento.PENDENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.agendar(agendamento));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("O agendamento deve terminar ate as 18:00.", exception.getReason());
    }

    @Test
    void deveBloquearConflitoPorIntervaloDeHorario() {
        Agendamento existente = agendamentoEm(dataUtil().atTime(9, 0), 60, StatusAgendamento.PENDENTE);
        Agendamento novo = agendamentoEm(dataUtil().atTime(9, 30), 40, StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of(existente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.agendar(novo));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Este barbeiro ja possui um agendamento neste intervalo de horario.", exception.getReason());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void devePermitirAgendamentoComecandoExatamenteNoFimDeOutro() {
        Agendamento existente = agendamentoEm(dataUtil().atTime(9, 0), 60, StatusAgendamento.PENDENTE);
        Agendamento novo = agendamentoEm(dataUtil().atTime(10, 0), 40, StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of(existente));
        when(agendamentoRepository.save(novo)).thenReturn(novo);

        Agendamento resultado = agendamentoService.agendar(novo);

        assertSame(novo, resultado);
        verify(agendamentoRepository).save(novo);
    }

    @Test
    void deveIgnorarAgendamentoCanceladoNoConflito() {
        Agendamento novo = agendamentoEm(dataUtil().atTime(9, 0), 40, StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of());
        when(agendamentoRepository.save(novo)).thenReturn(novo);

        Agendamento resultado = agendamentoService.agendar(novo);

        assertSame(novo, resultado);
        verify(agendamentoRepository).findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO);
    }

    @Test
    void deveCancelarAgendamentoComSucesso() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(9, 0), 40, StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.cancelar(1L);

        assertEquals(StatusAgendamento.CANCELADO, resultado.getStatus());
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void naoDeveCancelarAgendamentoJaCancelado() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(9, 0), 40, StatusAgendamento.CANCELADO);
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.cancelar(1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Agendamento ja esta cancelado.", exception.getReason());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void naoDeveCancelarAgendamentoConcluido() {
        Agendamento agendamento = agendamentoEm(dataUtil().atTime(9, 0), 40, StatusAgendamento.CONCLUIDO);
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.cancelar(1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Agendamento concluido nao pode ser cancelado.", exception.getReason());
    }

    @Test
    void deveGerarHorariosDisponiveis() {
        Usuario barbeiro = barbeiro();
        Servico servico = servico(40);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro));
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of());

        List<String> horarios = agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil());

        assertEquals(List.of("08:00", "08:40", "09:20", "10:00", "10:40", "11:20", "12:00",
                "12:40", "13:20", "14:00", "14:40", "15:20", "16:00", "16:40", "17:20"), horarios);
    }

    @Test
    void deveRemoverHorariosConflitantesDaDisponibilidade() {
        Usuario barbeiro = barbeiro();
        Servico servico = servico(40);
        Agendamento existente = agendamentoEm(dataUtil().atTime(9, 20), 40, StatusAgendamento.PENDENTE);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro));
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of(existente));

        List<String> horarios = agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil());

        assertFalse(horarios.contains("09:20"));
        assertTrue(horarios.contains("08:40"));
        assertTrue(horarios.contains("10:00"));
    }

    @Test
    void deveVoltarHorarioADisponibilidadeAposCancelamento() {
        Usuario barbeiro = barbeiro();
        Servico servico = servico(40);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro));
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO)).thenReturn(List.of());

        List<String> horarios = agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil());

        assertTrue(horarios.contains("09:20"));
        verify(agendamentoRepository).findByBarbeiroIdAndStatusNot(2L, StatusAgendamento.CANCELADO);
    }

    @Test
    void deveLancarErroQuandoBarbeiroNaoExiste() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil()));

        assertEquals("Barbeiro nao encontrado", exception.getMessage());
    }

    @Test
    void deveLancarErroQuandoUsuarioInformadoNaoEBarbeiro() {
        Usuario usuario = barbeiro();
        usuario.setPerfil("CLIENTE");
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Usuario informado nao e barbeiro.", exception.getReason());
    }

    @Test
    void deveLancarErroQuandoServicoNaoExiste() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro()));
        when(servicoRepository.findById(3L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil()));

        assertEquals("Servico nao encontrado", exception.getMessage());
    }

    @Test
    void deveLancarErroQuandoServicoTemDuracaoInvalida() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro()));
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(servico(0)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.listarHorariosDisponiveis(2L, 3L, dataUtil()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Servico deve ter duracao maior que zero.", exception.getReason());
    }

    @Test
    void deveBloquearHorariosDisponiveisAosDomingos() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro()));
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(servico(40)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> agendamentoService.listarHorariosDisponiveis(2L, 3L, proximoDomingo()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("A barbearia nao funciona aos domingos.", exception.getReason());
    }

    private Agendamento agendamentoEm(LocalDateTime dataEHora, int duracaoEmMinutos, StatusAgendamento status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setDataEHora(dataEHora);
        agendamento.setStatus(status);
        agendamento.setFormaDePagamento(FormaPagamento.PIX);
        agendamento.setCliente(cliente());
        agendamento.setBarbeiro(barbeiro());
        agendamento.setServico(servico(duracaoEmMinutos));
        return agendamento;
    }

    private Usuario cliente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Cliente");
        usuario.setEmail("cliente@example.com");
        usuario.setSenha("hash");
        usuario.setPerfil("CLIENTE");
        return usuario;
    }

    private Usuario barbeiro() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNome("Barbeiro");
        usuario.setEmail("barbeiro@example.com");
        usuario.setSenha("hash");
        usuario.setPerfil("BARBEIRO");
        return usuario;
    }

    private Servico servico(int duracaoEmMinutos) {
        Servico servico = new Servico();
        servico.setId(3L);
        servico.setNome("Corte");
        servico.setDescricao("Corte masculino");
        servico.setPreco(BigDecimal.valueOf(35));
        servico.setDuracaoEmMinutos(duracaoEmMinutos);
        return servico;
    }

    private LocalDate dataUtil() {
        LocalDate data = LocalDate.now().plusDays(7);
        while (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data;
    }

    private LocalDate proximoDomingo() {
        LocalDate data = LocalDate.now().plusDays(1);
        while (data.getDayOfWeek() != DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data;
    }
}
