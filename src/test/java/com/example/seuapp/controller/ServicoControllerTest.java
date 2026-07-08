package com.example.seuapp.controller;

import com.seuapp.controller.ServicoController;
import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.mapper.ServicoMapper;
import com.seuapp.model.Servico;
import com.seuapp.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoRepository servicoRepository;

    private ServicoController servicoController;

    @BeforeEach
    void setUp() {
        servicoController = new ServicoController(servicoRepository, new ServicoMapper());
    }

    @Test
    void deveCadastrarServicoValido() {
        ServicoCreateRequestDTO request = new ServicoCreateRequestDTO();
        request.setNome("Corte");
        request.setDescricao("Corte masculino");
        request.setPreco(BigDecimal.valueOf(35));
        request.setDuracaoEmMinutos(40);
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> {
            Servico servico = invocation.getArgument(0);
            servico.setId(1L);
            return servico;
        });

        ServicoResponseDTO response = servicoController.cadastrar(request);

        assertEquals(1L, response.getId());
        assertEquals("Corte", response.getNome());
        assertEquals(BigDecimal.valueOf(35), response.getPreco());
        assertEquals(40, response.getDuracaoEmMinutos());
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void deveRetornarNullQuandoServicoInexistente() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        ServicoResponseDTO response = servicoController.buscarPorId(99L);

        assertNull(response);
    }
}
