package br.com.chat.domain.ports.out;

import br.com.chat.domain.model.Message;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

public interface ChatCompletionPort {
    Flux<String> streamChatCompletion(List<Message> history, Consumer<String> onCompleteCallback);
}
