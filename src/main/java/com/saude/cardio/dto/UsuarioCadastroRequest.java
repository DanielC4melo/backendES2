package com.saude.cardio.dto;

import com.saude.cardio.model.Sexo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCadastroRequest {

    @NotBlank(message = "O campo nome é obrigatório.")
    @Size(max = 100, message = "O campo nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "O campo sobrenome é obrigatório.")
    @Size(max = 100, message = "O campo sobrenome deve ter no máximo 100 caracteres.")
    private String sobrenome;

    @NotBlank(message = "O campo email é obrigatório.")
    @Email(message = "O campo email deve ser um endereço de e-mail válido.")
    private String email;

    @NotBlank(message = "O campo telefone é obrigatório.")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "O campo telefone deve conter entre 10 e 15 dígitos numéricos."
    )
    private String telefone;

    @NotBlank(message = "O campo senha é obrigatório.")
    @Size(min = 8, message = "O campo senha deve ter no mínimo 8 caracteres.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
            message = "O campo senha deve conter letras maiúsculas, minúsculas, números e caracteres especiais."
    )
    private String senha;

    @NotBlank(message = "O campo confirmarSenha é obrigatório.")
    private String confirmarSenha;

    @NotNull(message = "O campo dataNascimento é obrigatório.")
    @Past(message = "O campo dataNascimento deve ser uma data no passado.")
    private LocalDate dataNascimento;

    @NotNull(message = "O campo sexo é obrigatório.")
    private Sexo sexo;

    @NotBlank(message = "O campo paisResidencia é obrigatório.")
    @Size(max = 100, message = "O campo paisResidencia deve ter no máximo 100 caracteres.")
    private String paisResidencia;
}
