package com.saude.cardio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "O campo email é obrigatório.")
    @Email(message = "O campo email deve ser um endereço de e-mail válido.")
    private String email;

    @NotBlank(message = "O campo senha é obrigatório.")
    private String senha;
}