package br.com.chat_ia.web.controller;

import br.com.chat_ia.domain.service.ChatbotService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping({"chat"})
@CrossOrigin(origins = "*")
public class ChatController {
    private ChatbotService chatBotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatBotService = chatbotService;
    }


    @GetMapping(value = "/v1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendQuestion(@RequestParam String userId, @RequestParam String question) {
        return chatBotService.sendQuestion(userId, question);
    };

    @GetMapping(value = "/v1/conversation", produces = MediaType.APPLICATION_JSON_VALUE)
    public String loadHistory(@RequestParam String userId) {
        return chatBotService.loadHistory(userId);
    };

    public void clearHistory() {};
}
