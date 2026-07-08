package com.seuapp.service;

import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import com.seuapp.repository.AgendamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private static final LocalTime HORA_ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FECHAMENTO = LocalTime.of(18, 0);

    private final AgendamentoRepository agendamentoRepository;

    public Agendamento agendar(Agendamento agendamento) {
        validarDisponibilidade(agendamento);
        return agendamentoRepository.save(agendamento);
    }

    public Agendamento cancelar(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento nao encontrado"));

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento ja esta cancelado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento concluido nao pode ser cancelado.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamento);
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
