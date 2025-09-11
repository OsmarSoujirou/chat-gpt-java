package br.com.chat.domain.model;

import java.time.LocalDateTime;

public class Message {
    private final LocalDateTime date;
    private final Role role;
    private final String content;

    public Message(Role role, String content) {
        this.date = LocalDateTime.now();
        this.role = role;
        this.content = content;
    }

    public String getDate() { return date.toString(); }
    public Role getRole() { return role; }
    public String getContent() { return content; }
}

