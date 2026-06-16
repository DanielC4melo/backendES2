package com.saude.cardio.model;

import jakarta.persistence.*;
import lombok.*;

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

    // Mantenha APENAS esta declaração de sintomas:
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