package com.saude.cardio.service;

import com.saude.cardio.exception.ExcecaoNegocio;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PressaoArterialParser {

    private static final Pattern FORMATO_BARRA = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");
    private static final Pattern FORMATO_POR = Pattern.compile("(\\d+)\\s*por\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    public int[] extrairSistolicaEDiastolica(String pressaoArterial) {
        Matcher matcherBarra = FORMATO_BARRA.matcher(pressaoArterial.trim());
        if (matcherBarra.matches()) {
            return normalizarValores(
                    Integer.parseInt(matcherBarra.group(1)),
                    Integer.parseInt(matcherBarra.group(2))
            );
        }

        Matcher matcherPor = FORMATO_POR.matcher(pressaoArterial.trim());
        if (matcherPor.matches()) {
            return normalizarValores(
                    Integer.parseInt(matcherPor.group(1)),
                    Integer.parseInt(matcherPor.group(2))
            );
        }

        throw new ExcecaoNegocio(400, "Dados enviados fora do intervalo técnico permitido.");
    }

    private int[] normalizarValores(int sistolica, int diastolica) {
        if (sistolica < 30 && diastolica < 30) {
            sistolica *= 10;
            diastolica *= 10;
        }
        return new int[]{sistolica, diastolica};
    }

    /**
     * Formata a média das pressões arteriais no padrão compacto do contrato (ex: "12 por 8").
     * Valores acima de 100 mmHg são convertidos para a escala reduzida dividindo por 10.
     */
    public String formatarMediaPressao(double mediaSistolica, double mediaDiastolica) {
        int sistolicaArredondada = (int) Math.round(mediaSistolica);
        int diastolicaArredondada = (int) Math.round(mediaDiastolica);

        if (sistolicaArredondada >= 100 && diastolicaArredondada >= 10) {
            return (sistolicaArredondada / 10) + " por " + (diastolicaArredondada / 10);
        }

        return sistolicaArredondada + " por " + diastolicaArredondada;
    }
}
