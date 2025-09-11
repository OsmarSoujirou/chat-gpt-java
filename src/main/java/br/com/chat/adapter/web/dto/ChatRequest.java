package br.com.chat.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(

        @NotBlank(message = "O ID do usuário (userId) não pode estar em branco.")
        String userId,

        @NotBlank(message = "A pergunta (question) не pode estar em branco.")
        @Size(max = 4000, message = "A pergunta não pode exceder 4000 caracteres.")
        String question
) {}