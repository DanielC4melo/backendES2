package com.saude.cardio.dto;

import com.saude.cardio.model.StatusAlerta;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroSaudeResponse {
    private Long idRegistro;
    private LocalDateTime dataHora;
    private StatusAlerta statusAlerta;
    private String pressaoArterial;
    private Integer frequenciaCardiaca;
    private Double nivelOxigenio;
    private Double pesoCorporal;
    private List<String> sintomas;
}