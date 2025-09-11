package br.com.chat.infra.openai;

import br.com.chat.domain.model.Message;
import br.com.chat.domain.ports.out.ChatCompletionPort;
import com.openai.client.OpenAIClient;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

@Service
public class OpenAIClientService implements ChatCompletionPort {

    private final OpenAIClient client;
    private final String systemPrompt;
    private final ChatModel chatModel;
    private final int assistantTruncateLimit;

    public OpenAIClientService(
            OpenAIClient client,
            @Value("${openai.system.prompt}") String systemPrompt,
            @Value("${openai.chat.model}") String modelName,
            @Value("${openai.assistant.content.truncate-limit}") int assistantTruncateLimit) {
        this.client = client;
        this.systemPrompt = systemPrompt;
        this.chatModel = ChatModel.of(modelName);
        this.assistantTruncateLimit = assistantTruncateLimit;
    }

    @Override
    public Flux<String> streamChatCompletion(List<Message> conversation, Consumer<String> onCompleteCallback) {
        List<ChatCompletionMessageParam> messages = conversation.stream()
                .map(this::toMessageParam)
                .toList();

        var params = ChatCompletionCreateParams.builder()
                .model(this.chatModel)
                .addSystemMessage(this.systemPrompt)
                .messages(messages)
                .build();

        StringBuilder responseBuilder = new StringBuilder();

        return Flux.create(sink -> {
            AsyncStreamResponse<ChatCompletionChunk> stream = client.async().chat().completions().createStreaming(params);

            stream.subscribe(chunk ->
                    chunk.choices().forEach(choice ->
                            choice.delta().content().ifPresent(text -> {
                                responseBuilder.append(text);
                                sink.next(text);
                            })
                    )
            );

            stream.onCompleteFuture().whenComplete((result, error) -> {
                if (error != null) {
                    sink.error(error);
                } else {
                    if (onCompleteCallback != null) {
                        onCompleteCallback.accept(responseBuilder.toString());
                    }
                    sink.complete();
                }
            });

            sink.onDispose(stream::close);
        });
    }

    private ChatCompletionMessageParam toMessageParam(Message message) {
        return switch (message.getRole()) {
            case SYSTEM -> ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder().content(message.getContent()).build()
            );
            case USER -> ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder().content(message.getContent()).build()
            );
            case ASSISTANT -> {
                String content = message.getContent();
                String truncatedContent = (content != null && content.length() > assistantTruncateLimit)
                        ? content.substring(0, assistantTruncateLimit) + "[truncated]"
                        : content;
                yield ChatCompletionMessageParam.ofAssistant(
                        ChatCompletionAssistantMessageParam.builder().content(truncatedContent).build()
                );
            }
        };
    }
}