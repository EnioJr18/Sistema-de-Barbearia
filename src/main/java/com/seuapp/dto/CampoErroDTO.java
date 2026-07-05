package com.seuapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CampoErroDTO {
    private String field;
    private String message;
}
