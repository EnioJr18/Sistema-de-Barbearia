package com.seuapp.service;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.mapper.ServicoMapper;
import com.seuapp.model.Servico;
import com.seuapp.repository.ServicoRepository;
import com.seuapp.service.ServicoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    private ServicoService servicoService;

    @BeforeEach
    void setUp() {
        servicoService = new ServicoService(servicoRepository, new ServicoMapper());
    }

    @Test
    void criarServicoValidoComSucesso() {
        ServicoCreateRequestDTO request = createRequest();
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> {
            Servico servico = invocation.getArgument(0);
            servico.setId(1L);
            return servico;
        });

        ServicoResponseDTO response = servicoService.cadastrar(request);

        assertEquals(1L, response.getId());
        assertEquals("Corte", response.getNome());
        assertEquals(BigDecimal.valueOf(35), response.getPreco());
        assertEquals(40, response.getDuracaoEmMinutos());
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void buscarServicoPorIdExistente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico(1L, "Corte")));

        ServicoResponseDTO response = servicoService.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Corte", response.getNome());
    }

    @Test
    void buscarServicoInexistenteRetornaEntityNotFoundException() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> servicoService.buscarPorId(99L));

        assertEquals("Servico nao encontrado", exception.getMessage());
    }

    @Test
    void atualizarServicoExistente() {
        Servico servico = servico(1L, "Corte");
        ServicoUpdateRequestDTO request = updateRequest();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(servico)).thenReturn(servico);

        ServicoResponseDTO response = servicoService.atualizar(1L, request);

        assertEquals("Barba", response.getNome());
        assertEquals(BigDecimal.valueOf(25), response.getPreco());
        assertEquals(30, response.getDuracaoEmMinutos());
    }

    @Test
    void atualizarServicoInexistenteRetornaEntityNotFoundException() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> servicoService.atualizar(99L, updateRequest()));

        assertEquals("Servico nao encontrado", exception.getMessage());
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void excluirServicoExistente() {
        when(servicoRepository.existsById(1L)).thenReturn(true);

        servicoService.deletar(1L);

        verify(servicoRepository).deleteById(1L);
    }

    @Test
    void excluirServicoInexistenteRetornaEntityNotFoundException() {
        when(servicoRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> servicoService.deletar(99L));

        assertEquals("Servico nao encontrado", exception.getMessage());
        verify(servicoRepository, never()).deleteById(any());
    }

    @Test
    void listarServicosComFiltroPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servico> pagina = new PageImpl<>(List.of(servico(1L, "Corte")));
        when(servicoRepository.findByNomeContainingIgnoreCase("cor", pageable)).thenReturn(pagina);

        Page<ServicoResponseDTO> response = servicoService.listarTodos(pageable, "cor");

        assertEquals(1, response.getTotalElements());
        assertEquals("Corte", response.getContent().getFirst().getNome());
        verify(servicoRepository).findByNomeContainingIgnoreCase("cor", pageable);
        verify(servicoRepository, never()).findAll(pageable);
    }

    @Test
    void listarServicosSemFiltroUsaFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servico> pagina = new PageImpl<>(List.of(servico(1L, "Corte")));
        when(servicoRepository.findAll(pageable)).thenReturn(pagina);

        Page<ServicoResponseDTO> response = servicoService.listarTodos(pageable, null);

        assertEquals(1, response.getTotalElements());
        assertEquals("Corte", response.getContent().getFirst().getNome());
        verify(servicoRepository).findAll(pageable);
    }

    private ServicoCreateRequestDTO createRequest() {
        ServicoCreateRequestDTO request = new ServicoCreateRequestDTO();
        request.setNome("Corte");
        request.setDescricao("Corte masculino");
        request.setPreco(BigDecimal.valueOf(35));
        request.setDuracaoEmMinutos(40);
        return request;
    }

    private ServicoUpdateRequestDTO updateRequest() {
        ServicoUpdateRequestDTO request = new ServicoUpdateRequestDTO();
        request.setNome("Barba");
        request.setDescricao("Barba completa");
        request.setPreco(BigDecimal.valueOf(25));
        request.setDuracaoEmMinutos(30);
        return request;
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
}
