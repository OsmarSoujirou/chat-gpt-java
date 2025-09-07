package br.com.chat_ia.domain.service;

import br.com.chat_ia.domain.model.Role;
import br.com.chat_ia.infra.openai.OpenAIClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotService {
    private final OpenAIClientService client;
    private static final String SYSTEM_PROMPT = "Você é um assistente útil, educado e claro.";

    // Todo: Colocar no H2
    private final List<String[]> conversation = new ArrayList<>();

    public ChatbotService(@Value("${openai.api.key}") String apiKey) {
        this.client = new OpenAIClientService(apiKey, SYSTEM_PROMPT);
    }

    public Flux<ServerSentEvent<String>> sendQuestion(String question) {
        addMessage(Role.USER.getValue(), question);
        return client.sendRequisitionChatCompletion(
                conversation,
                response -> addMessage(Role.SYSTEM.getValue(), response)
        );
    };

    private void addMessage(String role, String content) {
        conversation.add(new String[]{role, content});
    }


}
