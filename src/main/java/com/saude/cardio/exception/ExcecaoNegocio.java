package com.saude.cardio.exception;

import lombok.Getter;

@Getter
public class ExcecaoNegocio extends RuntimeException {

    private final int codigo;

    public ExcecaoNegocio(int codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }
}
