package com.saude.cardio.service;

import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.exception.ExcecaoNegocio;
import org.springframework.stereotype.Component;

@Component
public class ValidadorLimitesVitales {

    private static final int FREQUENCIA_MIN = 1;
    private static final int FREQUENCIA_MAX = 300;
    private static final double OXIGENIO_MIN = 1.0;
    private static final double OXIGENIO_MAX = 100.0;
    private static final double PESO_MIN = 1.0;
    private static final double PESO_MAX = 500.0;
    private static final int PRESSAO_SISTOLICA_MIN = 50;
    private static final int PRESSAO_SISTOLICA_MAX = 300;
    private static final int PRESSAO_DIASTOLICA_MIN = 30;
    private static final int PRESSAO_DIASTOLICA_MAX = 200;

    public void validar(RegistroSaudeRequest request, int pressaoSistolica, int pressaoDiastolica) {
        if (request.getFrequenciaCardiaca() < FREQUENCIA_MIN
                || request.getFrequenciaCardiaca() > FREQUENCIA_MAX) {
            throw new ExcecaoNegocio(400, "Dados enviados fora do intervalo técnico permitido.");
        }

        if (request.getNivelOxigenio() < OXIGENIO_MIN || request.getNivelOxigenio() > OXIGENIO_MAX) {
            throw new ExcecaoNegocio(400, "Dados enviados fora do intervalo técnico permitido.");
        }

        if (request.getPesoCorporal() < PESO_MIN || request.getPesoCorporal() > PESO_MAX) {
            throw new ExcecaoNegocio(400, "Dados enviados fora do intervalo técnico permitido.");
        }

        if (pressaoSistolica < PRESSAO_SISTOLICA_MIN
                || pressaoSistolica > PRESSAO_SISTOLICA_MAX
                || pressaoDiastolica < PRESSAO_DIASTOLICA_MIN
                || pressaoDiastolica > PRESSAO_DIASTOLICA_MAX
                || pressaoSistolica <= pressaoDiastolica) {
            throw new ExcecaoNegocio(400, "Dados enviados fora do intervalo técnico permitido.");
        }
    }
}
