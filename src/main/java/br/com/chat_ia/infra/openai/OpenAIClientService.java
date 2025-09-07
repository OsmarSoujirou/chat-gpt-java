package br.com.chat_ia.infra.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import org.springframework.http.codec.ServerSentEvent;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;


public class OpenAIClientService {

    private final OpenAIClient client;
    private final String systemPrompt;

    public OpenAIClientService(String apiKey, String systemPrompt) {
        this.systemPrompt = systemPrompt;
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }

    public Flux<ServerSentEvent<String>> sendRequisitionChatCompletion(List<String[]> conversation, Consumer<String> onComplete) {

        List<ChatCompletionMessageParam> messages = conversation.stream()
                .map(this::toMessageParam)
                .toList();

        var params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage(systemPrompt)
                .messages(messages)
                .build();

        StringBuilder responseBuilder = new StringBuilder();

        return Flux.create(sink -> {
            AsyncStreamResponse<ChatCompletionChunk> stream = client.async()
                    .chat()
                    .completions()
                    .createStreaming(params);

            stream.subscribe(chunk ->
                    chunk.choices().forEach(choice ->
                            choice.delta().content().ifPresent(text -> {
                                responseBuilder.append(text);
                                sink.next(ServerSentEvent.builder(text).build());
                            })
                    )
            );

            stream.onCompleteFuture().thenRun(() -> {
                if (onComplete != null) onComplete.accept(responseBuilder.toString());
                sink.complete();
            });

        });
    }

    private ChatCompletionMessageParam toMessageParam(String[] m) {
        String role = m[0];
        String content = m[1];

        if (role.equalsIgnoreCase("system")) {
            return ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder()
                            .content(content)
                            .build()
            );
        } else if (role.equalsIgnoreCase("user")) {
            return ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder()
                            .content(content)
                            .build()
            );
        } else if (role.equalsIgnoreCase("assistant")) {
            return ChatCompletionMessageParam.ofAssistant(
                    ChatCompletionAssistantMessageParam.builder()
                            .content(content)
                            .build()
            );
        } else {
            throw new IllegalStateException("Role desconhecida: " + role);
        }
    }
}
