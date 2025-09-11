package br.com.chat.adapter.web.dto;

public record  MessageResponse (
        String date,
        String role,
        String content
) {}
