package com.saude.cardio.service;

import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.dto.RegistroSaudeResponse;
import com.saude.cardio.model.StatusAlerta;
import com.saude.cardio.model.Usuario;
import com.saude.cardio.repository.RegistroSaudeRepository;
import com.saude.cardio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Teste unitário: triagem cardíaca com mocks no banco e JWT
@ExtendWith(MockitoExtension.class)
class RegistroSaudeServiceTest {

    @Mock
    private RegistroSaudeRepository registroSaudeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    private PressaoArterialParser pressaoArterialParser;
    private ValidadorLimitesVitales validadorLimitesVitales;
    private TriagemCardiacaService triagemCardiacaService;

    private RegistroSaudeService registroSaudeService;

    private Usuario usuario;

    @BeforeEach
    void configurarCenarioBase() {
        pressaoArterialParser = new PressaoArterialParser();
        validadorLimitesVitales = new ValidadorLimitesVitales();
        triagemCardiacaService = new TriagemCardiacaService();

        registroSaudeService = new RegistroSaudeService(
                registroSaudeRepository,
                usuarioRepository,
                jwtService,
                pressaoArterialParser,
                validadorLimitesVitales,
                triagemCardiacaService
        );

        usuario = Usuario.builder()
                .id(1L)
                .nome("Maria")
                .sobrenome("Oliveira")
                .email("maria@test.com")
                .build();
    }

    @Test
    @DisplayName("Deve classificar como CRÍTICO quando o paciente relatar dor no peito")
    void deveClassificarComoCriticoQuandoHouverDorNoPeito() {
        RegistroSaudeRequest requestCritico = new RegistroSaudeRequest();
        requestCritico.setPressaoArterial("120/80");
        requestCritico.setFrequenciaCardiaca(72);
        requestCritico.setNivelOxigenio(98.0);
        requestCritico.setPesoCorporal(70.0);
        requestCritico.setSintomas(List.of("dor no peito"));

        String authorization = "Bearer token-simulado";

        when(jwtService.extrairUsuarioIdDoToken(authorization)).thenReturn(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(registroSaudeRepository.save(any())).thenAnswer(invocacao -> {
            var registro = invocacao.getArgument(0, com.saude.cardio.model.RegistroSaude.class);
            registro.setId(10L);
            registro.setDataHora(LocalDateTime.now());
            return registro;
        });

        RegistroSaudeResponse resposta = registroSaudeService.registrar(1L, requestCritico);

        assertNotNull(resposta);
        assertEquals(StatusAlerta.CRITICO, resposta.getStatusAlerta());
        assertEquals(10L, resposta.getIdRegistro());
        assertNotNull(resposta.getDataHora());

        verify(registroSaudeRepository).save(any());
    }
}
