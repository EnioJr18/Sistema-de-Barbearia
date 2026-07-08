package com.seuapp.service;

import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import com.seuapp.model.Servico;
import com.seuapp.model.Usuario;
import com.seuapp.repository.AgendamentoRepository;
import com.seuapp.repository.ServicoRepository;
import com.seuapp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private static final LocalTime HORA_ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FECHAMENTO = LocalTime.of(18, 0);
    private static final DateTimeFormatter FORMATO_HORARIO = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public Agendamento agendar(Agendamento agendamento) {
        if (agendamento.getStatus() == null) {
            agendamento.setStatus(StatusAgendamento.PENDENTE);
        }
        validarDisponibilidade(agendamento);
        return agendamentoRepository.save(agendamento);
    }

    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento nao encontrado"));
    }

    public Agendamento atualizar(Long id, Agendamento agendamentoAtualizado) {
        buscarPorId(id);
        agendamentoAtualizado.setId(id);
        return agendamentoRepository.save(agendamentoAtualizado);
    }

    public void deletar(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Agendamento nao encontrado");
        }

        agendamentoRepository.deleteById(id);
    }

    public Agendamento cancelar(Long id) {
        Agendamento agendamento = buscarPorId(id);

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento ja esta cancelado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento concluido nao pode ser cancelado.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamento);
    }

    public List<String> listarHorariosDisponiveis(Long barbeiroId, Long servicoId, LocalDate data) {
        Usuario barbeiro = usuarioRepository.findById(barbeiroId)
                .orElseThrow(() -> new EntityNotFoundException("Barbeiro nao encontrado"));

        if (!"BARBEIRO".equals(barbeiro.getPerfil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario informado nao e barbeiro.");
        }

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new EntityNotFoundException("Servico nao encontrado"));

        if (servico.getDuracaoEmMinutos() == null || servico.getDuracaoEmMinutos() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servico deve ter duracao maior que zero.");
        }

        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A barbearia nao funciona aos domingos.");
        }

        List<Agendamento> agendamentosExistentes = agendamentoRepository
                .findByBarbeiroIdAndStatusNot(barbeiroId, StatusAgendamento.CANCELADO);

        List<String> horariosDisponiveis = new ArrayList<>();
        for (LocalTime horario = HORA_ABERTURA;
             !horario.plusMinutes(servico.getDuracaoEmMinutos()).isAfter(HORA_FECHAMENTO);
             horario = horario.plusMinutes(servico.getDuracaoEmMinutos())) {

            LocalDateTime inicio = data.atTime(horario);
            LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoEmMinutos());

            boolean temConflito = agendamentosExistentes.stream()
                    .anyMatch(existente -> existeSobreposicao(inicio, fim, existente));

            if (!temConflito) {
                horariosDisponiveis.add(horario.format(FORMATO_HORARIO));
            }
        }

        return horariosDisponiveis;
    }

    private void validarDisponibilidade(Agendamento agendamento) {
        LocalDateTime horaDesejada = agendamento.getDataEHora();
        LocalDateTime horaFinal = calcularHoraFinal(agendamento);
        Long idDoBarbeiro = agendamento.getBarbeiro().getId();

        if (horaDesejada.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e possivel realizar agendamentos no passado.");
        }

        if (horaDesejada.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A barbearia nao funciona aos domingos.");
        }

        if (horaDesejada.toLocalTime().isBefore(HORA_ABERTURA)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horario de funcionamento inicia as 08:00.");
        }

        if (horaFinal.toLocalTime().isAfter(HORA_FECHAMENTO) || !horaFinal.toLocalDate().equals(horaDesejada.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O agendamento deve terminar ate as 18:00.");
        }

        boolean existeSobreposicao = agendamentoRepository
                .findByBarbeiroIdAndStatusNot(idDoBarbeiro, StatusAgendamento.CANCELADO)
                .stream()
                .anyMatch(existente -> existeSobreposicao(horaDesejada, horaFinal, existente));

        if (existeSobreposicao) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este barbeiro ja possui um agendamento neste intervalo de horario.");
        }
    }

    private LocalDateTime calcularHoraFinal(Agendamento agendamento) {
        return agendamento.getDataEHora().plusMinutes(agendamento.getServico().getDuracaoEmMinutos());
    }

    private boolean existeSobreposicao(LocalDateTime novoInicio, LocalDateTime novoFim, Agendamento existente) {
        LocalDateTime existenteInicio = existente.getDataEHora();
        LocalDateTime existenteFim = calcularHoraFinal(existente);

        return novoInicio.isBefore(existenteFim) && novoFim.isAfter(existenteInicio);
    }
}
