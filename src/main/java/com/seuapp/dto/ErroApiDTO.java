package com.seuapp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErroApiDTO {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private List<CampoErroDTO> fields;
}
