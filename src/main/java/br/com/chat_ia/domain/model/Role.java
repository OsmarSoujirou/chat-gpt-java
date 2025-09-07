package br.com.chat_ia.domain.model;

import java.util.Arrays;

public enum Role {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converte String para Role, útil quando receber do cliente ou da API
     */
    public static Role fromValue(String value) {
        return Arrays.stream(Role.values())
                .filter(role -> role.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role desconhecida: " + value));
    }
}
