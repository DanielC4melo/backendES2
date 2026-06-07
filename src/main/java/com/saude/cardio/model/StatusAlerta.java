package com.saude.cardio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusAlerta {

    NORMAL("normal"),
    ATENCAO("atencao"),
    CRITICO("critico");

    private final String valor;

    StatusAlerta(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static StatusAlerta fromValor(String valor) {
        for (StatusAlerta status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor de statusAlerta invalido: " + valor);
    }
}
