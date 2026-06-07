package com.saude.cardio.service;

import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.model.StatusAlerta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TriagemCardiacaService {

    private static final int PRESSAO_SISTOLICA_ALTA = 140;
    private static final double OXIGENIO_CRITICO = 92.0;
    private static final double OXIGENIO_ATENCAO_MIN = 92.0;
    private static final double OXIGENIO_ATENCAO_MAX = 94.0;
    private static final int FREQUENCIA_CRITICA = 120;

    public StatusAlerta calcularStatus(RegistroSaudeRequest request, int pressaoSistolica) {
        List<String> sintomas = request.getSintomas() != null ? request.getSintomas() : List.of();

        if (contemSintoma(sintomas, "dor no peito")
                || request.getNivelOxigenio() < OXIGENIO_CRITICO
                || request.getFrequenciaCardiaca() > FREQUENCIA_CRITICA) {
            return StatusAlerta.CRITICO;
        }

        if (contemSintoma(sintomas, "falta de ar")
                || contemSintoma(sintomas, "tontura")
                || oxigenioNaFaixaAtencao(request.getNivelOxigenio())
                || pressaoSistolica >= PRESSAO_SISTOLICA_ALTA) {
            return StatusAlerta.ATENCAO;
        }

        return StatusAlerta.NORMAL;
    }

    private boolean oxigenioNaFaixaAtencao(double nivelOxigenio) {
        return nivelOxigenio >= OXIGENIO_ATENCAO_MIN && nivelOxigenio <= OXIGENIO_ATENCAO_MAX;
    }

    private boolean contemSintoma(List<String> sintomas, String sintomaEsperado) {
        return sintomas.stream()
                .anyMatch(sintoma -> sintoma != null && sintoma.equalsIgnoreCase(sintomaEsperado));
    }
}
