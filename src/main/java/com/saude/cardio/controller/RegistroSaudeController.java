package com.saude.cardio.controller;

import com.saude.cardio.dto.RegistroSaudeRequest;
import com.saude.cardio.dto.RegistroSaudeResponse;
import com.saude.cardio.service.RegistroSaudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios/{id}/registros-saude")
@RequiredArgsConstructor
public class RegistroSaudeController {

    private final RegistroSaudeService registroSaudeService;

    @PostMapping
    public ResponseEntity<RegistroSaudeResponse> registrar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RegistroSaudeRequest request) {
        RegistroSaudeResponse response = registroSaudeService.registrar(id, authorization, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
