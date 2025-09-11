package br.com.chat.domain.ports.out;

import br.com.chat.domain.model.Message;

import java.util.List;

public interface ConversationRepositoryPort {
    List<Message> findByUserId(String userId);
    void saveMessage(String userId, Message message);
    void deleteByUserId(String userId);
}
