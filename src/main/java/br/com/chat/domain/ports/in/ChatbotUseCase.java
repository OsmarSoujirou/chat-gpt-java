package br.com.chat.domain.ports.in;

import br.com.chat.domain.model.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatbotUseCase {
    Flux<String> getAnswer(String userId, String question);
    List<Message> loadHistory(String userId);
    void clearHistory(String userId);
}
