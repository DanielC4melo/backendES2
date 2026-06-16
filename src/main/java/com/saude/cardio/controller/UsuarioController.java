package com.saude.cardio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.saude.cardio.service.UsuarioService;
import com.saude.cardio.dto.UsuarioCadastroRequest;
import com.saude.cardio.dto.UsuarioResponse;
import com.saude.cardio.dto.LoginRequest;
import com.saude.cardio.dto.LoginResponse;

@RestController
@CrossOrigin(origins = "*") // Liberando o acesso para o nosso Ionic!
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Construtor para injetar o serviço corretamente (resolve o erro amarelo)
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }
}