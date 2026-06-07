package com.saude.cardio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Sexo {

    MASCULINO("masculino"),
    FEMININO("feminino"),
    OUTRO("outro");

    private final String valor;

    Sexo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static Sexo fromValor(String valor) {
        for (Sexo sexo : values()) {
            if (sexo.valor.equalsIgnoreCase(valor)) {
                return sexo;
            }
        }
        throw new IllegalArgumentException("Valor de sexo invalido: " + valor);
    }
}
