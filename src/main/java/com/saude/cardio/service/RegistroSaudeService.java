package com.saude.cardio.service;

import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.dto.RegistroSaudeResponse;
import com.saude.cardio.exception.ExcecaoNegocio;
import com.saude.cardio.model.RegistroSaude;
import com.saude.cardio.model.Usuario;
import com.saude.cardio.repository.RegistroSaudeRepository;
import com.saude.cardio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class RegistroSaudeService {

    private static final String MENSAGEM_ACESSO_NEGADO =
            "Acesso negado: você não tem permissão para registrar dados para este paciente.";

    private final RegistroSaudeRepository registroSaudeRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PressaoArterialParser pressaoArterialParser;
    private final ValidadorLimitesVitales validadorLimitesVitales;
    private final TriagemCardiacaService triagemCardiacaService;

    @Transactional
    public RegistroSaudeResponse registrar(Long usuarioId, String authorization, RegistroSaudeRequest request) {
        Long usuarioAutenticadoId = jwtService.extrairUsuarioIdDoToken(authorization);
        validarEscopo(usuarioId, usuarioAutenticadoId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ExcecaoNegocio(404, "Usuário não encontrado."));

        int[] pressao = pressaoArterialParser.extrairSistolicaEDiastolica(request.getPressaoArterial());
        validadorLimitesVitales.validar(request, pressao[0], pressao[1]);

        var statusAlerta = triagemCardiacaService.calcularStatus(request, pressao[0]);

        RegistroSaude registro = RegistroSaude.builder()
                .usuario(usuario)
                .pressaoArterial(request.getPressaoArterial())
                .frequenciaCardiaca(request.getFrequenciaCardiaca())
                .nivelOxigenio(request.getNivelOxigenio())
                .pesoCorporal(request.getPesoCorporal())
                .sintomas(request.getSintomas() != null ? new ArrayList<>(request.getSintomas()) : new ArrayList<>())
                .statusAlerta(statusAlerta)
                .build();

        RegistroSaude salvo = registroSaudeRepository.save(registro);

        return RegistroSaudeResponse.builder()
                .idRegistro(salvo.getId())
                .dataHora(salvo.getDataHora())
                .statusAlerta(salvo.getStatusAlerta())
                .build();
    }

    private void validarEscopo(Long usuarioIdRecurso, Long usuarioAutenticadoId) {
        if (!usuarioIdRecurso.equals(usuarioAutenticadoId)) {
            throw new ExcecaoNegocio(403, MENSAGEM_ACESSO_NEGADO);
        }
    }
}
