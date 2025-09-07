package br.com.chat_ia.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"chat"})
public class ChatController {
    private String chatBotService;

    public ChatController() {};

    @GetMapping
    public String index() {
        return "Chat ativo ✅";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong 🏓";
    }

    public void sendQuestion() {};

    public void loadHistory() {};

    public void clearHistory() {};
}
