package com.saude.cardio.controller;

import org.springframework.web.bind.annotation.*;
import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.dto.RegistroSaudeResponse;
import com.saude.cardio.service.RegistroSaudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/usuarios/{id}/registros-saude")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegistroSaudeController {

    private final RegistroSaudeService registroSaudeService;

    @PostMapping
    public ResponseEntity<RegistroSaudeResponse> registrar(
            @PathVariable Long id,
            @Valid @RequestBody RegistroSaudeRequest request) {
        // O serviço vai salvar os sintomas e o status de alerta
        RegistroSaudeResponse response = registroSaudeService.registrar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RegistroSaudeResponse>> listar(@PathVariable Long id) {
        // Busca a lista completa, incluindo os sintomas que agora estão no DTO
        List<RegistroSaudeResponse> registros = registroSaudeService.listarPorUsuario(id);
        return ResponseEntity.ok(registros);
    }
}