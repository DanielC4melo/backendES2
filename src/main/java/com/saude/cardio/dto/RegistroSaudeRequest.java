package com.saude.cardio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroSaudeRequest {

    @NotBlank(message = "O campo pressaoArterial é obrigatório.")
    private String pressaoArterial;

    @NotNull(message = "O campo frequenciaCardiaca é obrigatório.")
    private Integer frequenciaCardiaca;

    @NotNull(message = "O campo nivelOxigenio é obrigatório.")
    private Double nivelOxigenio;

    @NotNull(message = "O campo pesoCorporal é obrigatório.")
    private Double pesoCorporal;

    private List<String> sintomas = new ArrayList<>();
}
