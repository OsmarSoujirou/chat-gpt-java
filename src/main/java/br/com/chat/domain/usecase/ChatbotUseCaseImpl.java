package br.com.chat.domain.usecase;

import br.com.chat.domain.model.Message;
import br.com.chat.domain.model.Role;
import br.com.chat.domain.ports.in.ChatbotUseCase;
import br.com.chat.domain.ports.out.ChatCompletionPort;
import br.com.chat.domain.ports.out.ConversationRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatbotUseCaseImpl implements ChatbotUseCase {

    private final ChatCompletionPort chatClient;
    private final ConversationRepositoryPort conversationRepository;

    public ChatbotUseCaseImpl(ChatCompletionPort chatClient, ConversationRepositoryPort conversationRepository) {
        this.chatClient = chatClient;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Flux<String> getAnswer(String userId, String question) {

        List<Message> history = conversationRepository.findByUserId(userId);

        Message userMessage = new Message(Role.USER, question);
        history.add(userMessage);
        conversationRepository.saveMessage(userId, userMessage);

        return chatClient.streamChatCompletion(
                history,
                fullResponse -> {
                    Message assistantMessage = new Message(Role.ASSISTANT, fullResponse);
                    conversationRepository.saveMessage(userId, assistantMessage);
                }
        );
    }

    @Override
    public List<Message> loadHistory(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    @Override
    public void clearHistory(String userId) {
        conversationRepository.deleteByUserId(userId);
    }
}