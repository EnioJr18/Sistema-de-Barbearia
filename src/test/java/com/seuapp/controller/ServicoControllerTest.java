package com.seuapp.controller;

import com.seuapp.controller.ServicoController;
import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.service.ServicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoService servicoService;

    private ServicoController servicoController;

    @BeforeEach
    void setUp() {
        servicoController = new ServicoController(servicoService);
    }

    @Test
    void deveCadastrarServicoValido() {
        ServicoCreateRequestDTO request = new ServicoCreateRequestDTO();
        request.setNome("Corte");
        request.setDescricao("Corte masculino");
        request.setPreco(BigDecimal.valueOf(35));
        request.setDuracaoEmMinutos(40);
        ServicoResponseDTO serviceResponse = new ServicoResponseDTO();
        serviceResponse.setId(1L);
        serviceResponse.setNome("Corte");
        serviceResponse.setDescricao("Corte masculino");
        serviceResponse.setPreco(BigDecimal.valueOf(35));
        serviceResponse.setDuracaoEmMinutos(40);
        when(servicoService.cadastrar(request)).thenReturn(serviceResponse);

        ServicoResponseDTO response = servicoController.cadastrar(request);

        assertEquals(1L, response.getId());
        assertEquals("Corte", response.getNome());
        assertEquals(BigDecimal.valueOf(35), response.getPreco());
        assertEquals(40, response.getDuracaoEmMinutos());
        verify(servicoService).cadastrar(request);
    }

    @Test
    void deveBuscarServicoPorId() {
        ServicoResponseDTO serviceResponse = new ServicoResponseDTO();
        serviceResponse.setId(99L);
        serviceResponse.setNome("Corte");
        when(servicoService.buscarPorId(99L)).thenReturn(serviceResponse);

        ServicoResponseDTO response = servicoController.buscarPorId(99L);

        assertEquals(99L, response.getId());
        assertEquals("Corte", response.getNome());
        verify(servicoService).buscarPorId(99L);
    }
}
