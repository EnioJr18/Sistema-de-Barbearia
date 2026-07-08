package com.seuapp.service;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.mapper.ServicoMapper;
import com.seuapp.model.Servico;
import com.seuapp.repository.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    public Page<ServicoResponseDTO> listarTodos(Pageable pageable, String nome) {
        Page<Servico> paginaDeServicos;

        if (nome != null) {
            paginaDeServicos = servicoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            paginaDeServicos = servicoRepository.findAll(pageable);
        }

        return paginaDeServicos.map(servicoMapper::toResponse);
    }

    public ServicoResponseDTO cadastrar(ServicoCreateRequestDTO request) {
        Servico servico = servicoMapper.toEntity(request);
        return servicoMapper.toResponse(servicoRepository.save(servico));
    }

    public ServicoResponseDTO buscarPorId(Long id) {
        return servicoRepository.findById(id)
                .map(servicoMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Servico nao encontrado"));
    }

    public ServicoResponseDTO atualizar(Long id, ServicoUpdateRequestDTO request) {
        return servicoRepository.findById(id)
                .map(servico -> {
                    servicoMapper.updateEntity(servico, request);
                    return servicoMapper.toResponse(servicoRepository.save(servico));
                })
                .orElseThrow(() -> new EntityNotFoundException("Servico nao encontrado"));
    }

    public void deletar(Long id) {
        if (!servicoRepository.existsById(id)) {
            throw new EntityNotFoundException("Servico nao encontrado");
        }

        servicoRepository.deleteById(id);
    }
}
