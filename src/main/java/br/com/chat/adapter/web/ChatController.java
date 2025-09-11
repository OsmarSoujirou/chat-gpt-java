package br.com.chat.adapter.web;

import br.com.chat.domain.model.Message;
import br.com.chat.domain.ports.in.ChatbotUseCase;

import br.com.chat.adapter.web.dto.ChatRequest;
import br.com.chat.adapter.web.dto.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping({"chat/v1"})
@CrossOrigin(origins = "*")
public class ChatController {
    private final ChatbotUseCase chatbotUseCase;

    public ChatController(ChatbotUseCase chatbotUseCase) {
        this.chatbotUseCase = chatbotUseCase;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ChatRequest request) {

        return chatbotUseCase.getAnswer(request.userId(), request.question())
                .map(content -> {
                    String formattedContent = content.replaceAll("^ ", "[S]");;
                    return ServerSentEvent.<String>builder().data(formattedContent).build();
                })
                .concatWith(Flux.just(ServerSentEvent.builder("[DONE]").build()));
    }

    @GetMapping("/history/{userId}")
    public List<MessageResponse> getHistory(@PathVariable String userId) {
        List<Message> domainMessages = chatbotUseCase.loadHistory(userId);

        return domainMessages.stream()
                .map(msg -> new MessageResponse(
                        msg.getDate(),
                        msg.getRole().getValue(),
                        msg.getContent()
                ))
                .toList();
    }

    @DeleteMapping("/history/{userId}")
    public void clearHistory(@PathVariable String userId) {
        chatbotUseCase.clearHistory(userId);
    };
}
