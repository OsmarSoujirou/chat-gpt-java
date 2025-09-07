package br.com.chat_ia.web.controller;

import br.com.chat_ia.domain.service.ChatbotService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping({"chat"})
public class ChatController {
    private ChatbotService chatBotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatBotService = chatbotService;
    }

    @GetMapping
    public String index() {
        return "Chat ativo ✅";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong 🏓";
    }

    @PostMapping(value = "/v1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendQuestion(@RequestBody String question) {
        return chatBotService.sendQuestion(question);
    };

    public void loadHistory() {};

    public void clearHistory() {};
}
