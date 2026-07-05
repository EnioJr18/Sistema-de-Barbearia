package com.seuapp.mapper;

import com.seuapp.dto.ServicoCreateRequestDTO;
import com.seuapp.dto.ServicoResponseDTO;
import com.seuapp.dto.ServicoResumoResponseDTO;
import com.seuapp.dto.ServicoUpdateRequestDTO;
import com.seuapp.model.Servico;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public Servico toEntity(ServicoCreateRequestDTO request) {
        Servico servico = new Servico();
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setPreco(request.getPreco());
        servico.setDuracaoEmMinutos(request.getDuracaoEmMinutos());
        return servico;
    }

    public void updateEntity(Servico servico, ServicoUpdateRequestDTO request) {
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setPreco(request.getPreco());
        servico.setDuracaoEmMinutos(request.getDuracaoEmMinutos());
    }

    public ServicoResponseDTO toResponse(Servico servico) {
        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(servico.getId());
        response.setNome(servico.getNome());
        response.setDescricao(servico.getDescricao());
        response.setPreco(servico.getPreco());
        response.setDuracaoEmMinutos(servico.getDuracaoEmMinutos());
        return response;
    }

    public ServicoResumoResponseDTO toResumoResponse(Servico servico) {
        ServicoResumoResponseDTO response = new ServicoResumoResponseDTO();
        response.setId(servico.getId());
        response.setNome(servico.getNome());
        response.setPreco(servico.getPreco());
        return response;
    }
}
