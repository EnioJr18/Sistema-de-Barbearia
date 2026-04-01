package com.seuapp.service;

import com.seuapp.model.Agendamento;
import com.seuapp.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;

    public Agendamento agendar(Agendamento agendamento) {
        LocalDateTime horaDesejada = agendamento.getDataEHora();
        Long idDoBarbeiro = agendamento.getBarbeiro().getId();

        // --- INÍCIO DAS REGRAS DE NEGÓCIO ---

        // Regra 1: Bloquear datas no passado
        if (horaDesejada.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível realizar agendamentos no passado.");
        }

        // Regra 2: Bloquear agendamentos aos domingos, personalizável
        if (horaDesejada.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A barbearia não funciona aos domingos.");
        }

        // Regra 3: Validar horário comercial (ex: das 08:00 até o último horário às 18:00), também personalizável
        int hora = horaDesejada.getHour();
        if (hora < 8 || hora > 18) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário de funcionamento é das 08:00 às 19:00.");
        }

        // Regra 4: Evitar choque de horários
        boolean horarioOcupado = agendamentoRepository.existsByBarbeiroIdAndDataEHora(idDoBarbeiro, horaDesejada);
        if (horarioOcupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este barbeiro já possui um agendamento neste horário.");
        }

        return agendamentoRepository.save(agendamento);
    }
}