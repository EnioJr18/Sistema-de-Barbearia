package com.seuapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenciaRequestDTO {
    @NotNull(message = "id e obrigatorio")
    @Positive(message = "id deve ser maior que zero")
    private Long id;
}
