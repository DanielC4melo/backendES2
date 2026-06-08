package com.saude.cardio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioDiarioResponse {

    private LocalDate data;
    private String mediaPressao;
    private double mediaFrequencia;
    private double variacaoPeso;
}
