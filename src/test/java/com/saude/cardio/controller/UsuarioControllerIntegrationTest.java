package com.saude.cardio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saude.cardio.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de integração: sobe o Spring inteiro e bate na API de verdade
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve cadastrar usuário no H2 e autenticar via login retornando token JWT")
    void deveCadastrarUsuarioEPersistirNoH2EAutenticarComToken() throws Exception {
        String payloadCadastro = """
                {
                  "nome": "Ana",
                  "sobrenome": "Costa",
                  "email": "ana.integracao@test.com",
                  "telefone": "11977776666",
                  "senha": "Senha@123",
                  "confirmarSenha": "Senha@123",
                  "dataNascimento": "1988-03-20",
                  "sexo": "feminino",
                  "paisResidencia": "Brasil"
                }
                """;

        MvcResult resultadoCadastro = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana.integracao@test.com"))
                .andExpect(jsonPath("$.dataCriacao").exists())
                .andReturn();

        assertTrue(usuarioRepository.existsByEmail("ana.integracao@test.com"));
        assertEquals("Ana", usuarioRepository.findByEmail("ana.integracao@test.com")
                .orElseThrow()
                .getNome());

        JsonNode corpoCadastro = objectMapper.readTree(resultadoCadastro.getResponse().getContentAsString());
        long idGerado = corpoCadastro.get("id").asLong();
        assertTrue(idGerado > 0);

        String payloadLogin = """
                {
                  "email": "ana.integracao@test.com",
                  "password": "Senha@123"
                }
                """;

        MvcResult resultadoLogin = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").value((int) idGerado))
                .andExpect(jsonPath("$.email").value("ana.integracao@test.com"))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Costa"))
                .andReturn();

        JsonNode corpoLogin = objectMapper.readTree(resultadoLogin.getResponse().getContentAsString());
        String token = corpoLogin.get("token").asText();

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }
}
