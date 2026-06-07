package com.saude.cardio.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registros_saude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String pressaoArterial;

    @Column(nullable = false)
    private int frequenciaCardiaca;

    @Column(nullable = false)
    private double nivelOxigenio;

    @Column(nullable = false)
    private double pesoCorporal;

    @ElementCollection
    @CollectionTable(name = "registro_saude_sintomas", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "sintoma")
    @Builder.Default
    private List<String> sintomas = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlerta statusAlerta;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }
}
