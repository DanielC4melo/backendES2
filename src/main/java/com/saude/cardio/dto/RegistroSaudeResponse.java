package com.saude.cardio.dto;

import com.saude.cardio.model.StatusAlerta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroSaudeResponse {

    private Long idRegistro;
    private LocalDateTime dataHora;
    private StatusAlerta statusAlerta;
}
