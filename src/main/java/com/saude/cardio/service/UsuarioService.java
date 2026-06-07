package com.saude.cardio.service;

import com.saude.cardio.dto.UsuarioCadastroRequest;
import com.saude.cardio.dto.UsuarioResponse;
import com.saude.cardio.exception.ExcecaoNegocio;
import com.saude.cardio.model.Usuario;
import com.saude.cardio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrar(UsuarioCadastroRequest request) {
        validarSenhasIguais(request);
        validarEmailOuTelefoneUnicos(request);

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .sobrenome(request.getSobrenome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .senha(passwordEncoder.encode(request.getSenha()))
                .dataNascimento(request.getDataNascimento())
                .sexo(request.getSexo())
                .paisResidencia(request.getPaisResidencia())
                .build();

        Usuario salvo = usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .id(salvo.getId())
                .nome(salvo.getNome())
                .email(salvo.getEmail())
                .dataCriacao(salvo.getDataCriacao())
                .build();
    }

    private void validarSenhasIguais(UsuarioCadastroRequest request) {
        if (!request.getSenha().equals(request.getConfirmarSenha())) {
            throw new ExcecaoNegocio(422, "O campo senha e confirmação de senha não são iguais.");
        }
    }

    private void validarEmailOuTelefoneUnicos(UsuarioCadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())
                || usuarioRepository.existsByTelefone(request.getTelefone())) {
            throw new ExcecaoNegocio(409, "E-mail ou telefone já cadastrado no sistema.");
        }
    }
}
