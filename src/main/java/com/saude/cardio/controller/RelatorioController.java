package com.saude.cardio.controller;

import com.saude.cardio.dto.RelatorioDiarioResponse;
import com.saude.cardio.service.RegistroSaudeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios/{id}/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RegistroSaudeService registroSaudeService;

    @GetMapping
    public ResponseEntity<List<RelatorioDiarioResponse>> gerarRelatorios(
            @PathVariable Long id,
            @RequestParam String inicio,
            @RequestParam String fim) {
        List<RelatorioDiarioResponse> relatorios =
                registroSaudeService.gerarRelatorios(id, inicio, fim);
        return ResponseEntity.ok(relatorios);
    }
}
