package com.seuapp.mapper;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoResumoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.mapper.ServicoMapper;
import com.seuapp.model.Servico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicoMapperTest {

    private final ServicoMapper servicoMapper = new ServicoMapper();

    @Test
    void deveConverterCreateRequestParaEntidade() {
        ServicoCreateRequestDTO request = new ServicoCreateRequestDTO();
        request.setNome("Corte");
        request.setDescricao("Corte masculino");
        request.setPreco(BigDecimal.valueOf(35));
        request.setDuracaoEmMinutos(40);

        Servico servico = servicoMapper.toEntity(request);

        assertEquals("Corte", servico.getNome());
        assertEquals("Corte masculino", servico.getDescricao());
        assertEquals(BigDecimal.valueOf(35), servico.getPreco());
        assertEquals(40, servico.getDuracaoEmMinutos());
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Servico servico = servico();

        ServicoResponseDTO response = servicoMapper.toResponse(servico);

        assertEquals(1L, response.getId());
        assertEquals("Corte", response.getNome());
        assertEquals("Corte masculino", response.getDescricao());
        assertEquals(BigDecimal.valueOf(35), response.getPreco());
        assertEquals(40, response.getDuracaoEmMinutos());
    }

    @Test
    void deveConverterEntidadeParaResumoResponse() {
        Servico servico = servico();

        ServicoResumoResponseDTO response = servicoMapper.toResumoResponse(servico);

        assertEquals(1L, response.getId());
        assertEquals("Corte", response.getNome());
        assertEquals(BigDecimal.valueOf(35), response.getPreco());
    }

    @Test
    void deveAtualizarEntidadeComUpdateRequest() {
        Servico servico = servico();
        ServicoUpdateRequestDTO request = new ServicoUpdateRequestDTO();
        request.setNome("Barba");
        request.setDescricao("Barba completa");
        request.setPreco(BigDecimal.valueOf(25));
        request.setDuracaoEmMinutos(30);

        servicoMapper.updateEntity(servico, request);

        assertEquals("Barba", servico.getNome());
        assertEquals("Barba completa", servico.getDescricao());
        assertEquals(BigDecimal.valueOf(25), servico.getPreco());
        assertEquals(30, servico.getDuracaoEmMinutos());
    }

    private Servico servico() {
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte");
        servico.setDescricao("Corte masculino");
        servico.setPreco(BigDecimal.valueOf(35));
        servico.setDuracaoEmMinutos(40);
        return servico;
    }
}
