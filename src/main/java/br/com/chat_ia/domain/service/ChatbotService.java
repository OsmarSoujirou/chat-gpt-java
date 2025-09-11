package br.com.chat_ia.domain.service;

import br.com.chat_ia.domain.model.Message;
import br.com.chat_ia.domain.model.Role;
import br.com.chat_ia.infra.openai.OpenAIClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
public class ChatbotService {
    private final OpenAIClientService client;
    private static final String SYSTEM_PROMPT = "Você é um assistente útil, educado e claro.";

    // Todo: Colocar no H2
    private final Map<String, List<Message>> conversationMap = new ConcurrentHashMap<>();

    public ChatbotService(@Value("${openai.api.key}") String apiKey) {
        this.client = new OpenAIClientService(apiKey, SYSTEM_PROMPT);
    }

    public Flux<ServerSentEvent<String>> sendQuestion(String userId, String question) {
        List<Message> history = conversationMap.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));

        addMessage(history, Role.USER.getValue(), question);
        return Flux.concat(
                client.sendRequisitionChatCompletion(
                        history,
                        response -> addMessage(history, Role.ASSISTANT.getValue(), response)
                ),
                Flux.just(ServerSentEvent.builder("[DONE]").build())
        );
    };

    private void addMessage(List<Message> history, String role, String content) {
        history.add(new Message(role, content));
    }


    public String loadHistory(String userId) {
        List<Message> history = conversationMap.getOrDefault(userId, Collections.emptyList());

        return history.stream()
                .map(msg -> String.format(
                        "{\"date\":\"%s\",\"role\":\"%s\",\"content\":\"%s\"}",
                        escapeJson(msg.getDate()),
                        escapeJson(msg.getRole()),
                        escapeJson(msg.getContent())
                ))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}
