package br.com.chat_ia.domain.model;

import java.time.LocalDateTime;

public class Message {
    private final LocalDateTime date;
    private final String role;
    private final String content;

    public Message(String role, String content) {
        this.date = LocalDateTime.now();
        this.role = role;
        this.content = content;
    }

    public String getDate() { return date.toString(); }
    public String getRole() { return role; }
    public String getContent() { return content; }
}

