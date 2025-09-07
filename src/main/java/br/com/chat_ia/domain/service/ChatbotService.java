package br.com.chat_ia.domain.service;

import br.com.chat_ia.infra.openai.OpenAIClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatbotService {
    private OpenAIClientService client;
    private static final String SYSTEM_PROMPT = "Você é um assistente útil, educado e claro.";

    public ChatbotService(@Value("${openai.api.key}") String apiKey) {
        this.client = new OpenAIClientService(apiKey, SYSTEM_PROMPT);
    }

    public Flux<ServerSentEvent<String>> sendQuestion(String question) {
        return client.sendRequisitionChatCompletion(question);
    };


}
