package com.saude.cardio.service;

import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.dto.RegistroSaudeResponse;
import com.saude.cardio.dto.RelatorioDiarioResponse;
import com.saude.cardio.exception.ExcecaoNegocio;
import com.saude.cardio.model.RegistroSaude;
import com.saude.cardio.model.Usuario;
import com.saude.cardio.repository.RegistroSaudeRepository;
import com.saude.cardio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroSaudeService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String MENSAGEM_ACESSO_NEGADO =
            "Acesso negado: você não tem permissão para registrar dados para este paciente.";
    private static final String MENSAGEM_FORMATO_DATA_INVALIDO =
            "Formato de data inválido. Use o formato YYYY-MM-DD.";
    private static final String MENSAGEM_NENHUM_REGISTRO =
            "Nenhum registro encontrado para o período informado.";

    private final RegistroSaudeRepository registroSaudeRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PressaoArterialParser pressaoArterialParser;
    private final ValidadorLimitesVitales validadorLimitesVitales;
    private final TriagemCardiacaService triagemCardiacaService;

    @Transactional
    public RegistroSaudeResponse registrar(Long usuarioId, RegistroSaudeRequest request) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ExcecaoNegocio(401, "Usuário não autenticado"));

        validarEscopo(usuarioId, usuarioAutenticado.getId());
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
                .pressaoArterial(salvo.getPressaoArterial())
                .frequenciaCardiaca(salvo.getFrequenciaCardiaca())
                .nivelOxigenio(salvo.getNivelOxigenio())
                .pesoCorporal(salvo.getPesoCorporal())
                .sintomas(salvo.getSintomas())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RegistroSaudeResponse> listarPorUsuario(Long usuarioId) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ExcecaoNegocio(401, "Usuário não autenticado"));

        validarEscopo(usuarioId, usuarioAutenticado.getId());

        List<RegistroSaude> registros = registroSaudeRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);

        return registros.stream()
                .map(r -> RegistroSaudeResponse.builder()
                        .idRegistro(r.getId())
                        .dataHora(r.getDataHora())
                        .pressaoArterial(r.getPressaoArterial())
                        .frequenciaCardiaca(r.getFrequenciaCardiaca())
                        .nivelOxigenio(r.getNivelOxigenio())
                        .pesoCorporal(r.getPesoCorporal())
                        .statusAlerta(r.getStatusAlerta())
                        .sintomas(r.getSintomas()) // <--- GARANTINDO QUE OS SINTOMAS VÃO PARA O FRONT
                        .build())
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<RelatorioDiarioResponse> gerarRelatorios(Long usuarioId, String inicio, String fim) {
        return null;
    }

    private double calcularVariacaoPesoPeriodo(List<RegistroSaude> registros) {
        return registros.get(registros.size() - 1).getPesoCorporal() - registros.get(0).getPesoCorporal();
    }

    private RelatorioDiarioResponse construirRelatorioDiario(LocalDate data, List<RegistroSaude> registrosDoDia, double variacaoPesoPeriodo) {
        return null;
    }

    private LocalDate parsearData(String dataTexto) {
        try { return LocalDate.parse(dataTexto, FORMATO_DATA); }
        catch (DateTimeParseException ex) { throw new ExcecaoNegocio(400, MENSAGEM_FORMATO_DATA_INVALIDO); }
    }

    private void validarEscopo(Long usuarioIdRecurso, Long usuarioAutenticadoId) {
        if (!usuarioIdRecurso.equals(usuarioAutenticadoId)) throw new ExcecaoNegocio(403, MENSAGEM_ACESSO_NEGADO);
    }
}