package com.saude.cardio.service;

import com.saude.cardio.dto.UsuarioCadastroRequest;
import com.saude.cardio.dto.UsuarioResponse;
import com.saude.cardio.exception.ExcecaoNegocio;
import com.saude.cardio.model.Sexo;
import com.saude.cardio.model.Usuario;
import com.saude.cardio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Teste unitário: só o service, dependências mockadas
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioCadastroRequest requestValido;

    @BeforeEach
    void configurarCenarioBase() {
        requestValido = new UsuarioCadastroRequest(
                "João",
                "Silva",
                "joao@test.com",
                "11999998888",
                "Senha@123",
                "Senha@123",
                LocalDate.of(1990, 5, 15),
                Sexo.MASCULINO,
                "Brasil"
        );
    }

    @Test
    @DisplayName("Deve lançar exceção 422 quando senha e confirmação de senha forem diferentes")
    void deveLancarExcecao422QuandoSenhasForemDiferentes() {
        requestValido.setConfirmarSenha("OutraSenha@456");

        ExcecaoNegocio excecao = assertThrows(ExcecaoNegocio.class,
                () -> usuarioService.cadastrar(requestValido));

        assertEquals(422, excecao.getCodigo());
        assertEquals("O campo senha e confirmação de senha não são iguais.", excecao.getMessage());

        // não deve nem tentar salvar no banco
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve criptografar a senha e persistir usuário quando os dados forem válidos")
    void deveCriptografarSenhaEPersistirUsuarioQuandoDadosForemValidos() {
        String senhaPlana = requestValido.getSenha();
        String senhaCriptografada = "$2a$10$hashBcryptSimuladoParaTesteUnitario";

        when(usuarioRepository.existsByEmail(requestValido.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(requestValido.getTelefone())).thenReturn(false);
        when(passwordEncoder.encode(senhaPlana)).thenReturn(senhaCriptografada);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario usuario = invocacao.getArgument(0);
            usuario.setId(1L);
            usuario.setDataCriacao(LocalDateTime.now());
            return usuario;
        });

        UsuarioResponse resposta = usuarioService.cadastrar(requestValido);

        assertNotNull(resposta);
        assertEquals(1L, resposta.getId());
        assertEquals("João", resposta.getNome());
        assertEquals("joao@test.com", resposta.getEmail());
        assertNotNull(resposta.getDataCriacao());

        verify(passwordEncoder).encode(senhaPlana);

        ArgumentCaptor<Usuario> captorUsuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captorUsuario.capture());

        Usuario usuarioPersistido = captorUsuario.getValue();
        assertEquals(senhaCriptografada, usuarioPersistido.getSenha());
        assertNotEquals(senhaPlana, usuarioPersistido.getSenha());
    }
}
