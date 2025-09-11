package br.com.chat.infra.persistence;

import br.com.chat.domain.model.Message;
import br.com.chat.domain.ports.out.ConversationRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryConversationRepository implements ConversationRepositoryPort {

    private final Map<String, List<Message>> conversationMap = new ConcurrentHashMap<>();

    @Override
    public List<Message> findByUserId(String userId) {
        return new ArrayList<>(conversationMap.getOrDefault(userId, Collections.emptyList()));
    }

    @Override
    public void saveMessage(String userId, Message message) {
        List<Message> history = conversationMap.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));
        history.add(message);
    }

    @Override
    public void deleteByUserId(String userId) {
        conversationMap.remove(userId);
    }
}