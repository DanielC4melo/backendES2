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
                .build();
    }

    @Transactional(readOnly = true)
    public List<RelatorioDiarioResponse> gerarRelatorios(
            Long usuarioId,
            String inicio,
            String fim) {


        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ExcecaoNegocio(401, "Usuário não autenticado"));
        validarEscopo(usuarioId, usuarioAutenticado.getId());

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ExcecaoNegocio(404, "Usuário não encontrado.");
        }

        LocalDate dataInicio = parsearData(inicio);
        LocalDate dataFim = parsearData(fim);

        // Considera o dia inteiro: de 00:00:00 até 23:59:59.999999999
        LocalDateTime inicioPeriodo = dataInicio.atStartOfDay();
        LocalDateTime fimPeriodo = dataFim.atTime(LocalTime.MAX);

        List<RegistroSaude> registros = registroSaudeRepository
                .findByUsuarioIdAndDataHoraBetweenOrderByDataHoraAsc(usuarioId, inicioPeriodo, fimPeriodo);

        if (registros.isEmpty()) {
            throw new ExcecaoNegocio(404, MENSAGEM_NENHUM_REGISTRO);
        }

        // Variação de peso do período: peso mais recente menos o primeiro peso cronológico
        double variacaoPesoPeriodo = calcularVariacaoPesoPeriodo(registros);

        // Agrupa os registros por dia civil (LocalDate) para consolidar métricas diárias
        Map<LocalDate, List<RegistroSaude>> registrosPorDia = registros.stream()
                .collect(Collectors.groupingBy(registro -> registro.getDataHora().toLocalDate()));

        return registrosPorDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entrada -> construirRelatorioDiario(entrada.getKey(), entrada.getValue(), variacaoPesoPeriodo))
                .toList();
    }

    /**
     * Calcula a variação de peso no período informado.
     * Fórmula: peso_mais_recente - peso_primeiro (ambos ordenados por dataHora ascendente).
     * Resultado positivo indica ganho de peso; negativo indica perda.
     */
    private double calcularVariacaoPesoPeriodo(List<RegistroSaude> registros) {
        RegistroSaude primeiroRegistro = registros.get(0);
        RegistroSaude registroMaisRecente = registros.get(registros.size() - 1);

        return registroMaisRecente.getPesoCorporal() - primeiroRegistro.getPesoCorporal();
    }

    /**
     * Consolida as métricas estatísticas de um único dia a partir de todos os registros daquele dia.
     */
    private RelatorioDiarioResponse construirRelatorioDiario(
            LocalDate data,
            List<RegistroSaude> registrosDoDia,
            double variacaoPesoPeriodo) {

        // Média aritmética da frequência cardíaca: soma / quantidade
        DoubleSummaryStatistics estatisticasFrequencia = registrosDoDia.stream()
                .mapToDouble(RegistroSaude::getFrequenciaCardiaca)
                .summaryStatistics();

        double mediaFrequencia = estatisticasFrequencia.getAverage();

        // Extrai sistólica e diastólica de cada registro, depois calcula a média de cada componente
        double mediaSistolica = registrosDoDia.stream()
                .mapToInt(registro -> pressaoArterialParser.extrairSistolicaEDiastolica(registro.getPressaoArterial())[0])
                .average()
                .orElse(0.0);

        double mediaDiastolica = registrosDoDia.stream()
                .mapToInt(registro -> pressaoArterialParser.extrairSistolicaEDiastolica(registro.getPressaoArterial())[1])
                .average()
                .orElse(0.0);

        String mediaPressao = pressaoArterialParser.formatarMediaPressao(mediaSistolica, mediaDiastolica);

        return RelatorioDiarioResponse.builder()
                .data(data)
                .mediaPressao(mediaPressao)
                .mediaFrequencia(mediaFrequencia)
                .variacaoPeso(variacaoPesoPeriodo)
                .build();
    }

    private LocalDate parsearData(String dataTexto) {
        try {
            return LocalDate.parse(dataTexto, FORMATO_DATA);
        } catch (DateTimeParseException ex) {
            throw new ExcecaoNegocio(400, MENSAGEM_FORMATO_DATA_INVALIDO);
        }
    }

    private void validarEscopo(Long usuarioIdRecurso, Long usuarioAutenticadoId) {
        if (!usuarioIdRecurso.equals(usuarioAutenticadoId)) {
            throw new ExcecaoNegocio(403, MENSAGEM_ACESSO_NEGADO);
        }
    }
}
