package br.com.chat_ia.infra.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import org.springframework.http.codec.ServerSentEvent;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static com.openai.models.chat.completions.ChatCompletionRole.*;


public class OpenAIClientService {

    private final OpenAIClient client;
    private final String systemPrompt;

    // TODO: Carregar historico do H2 talvez mover para domain service tbm
    private final List<Conversation> conversationHistory = new ArrayList<>();
    // TODO: Separar DTO
    private record Conversation(String role, String content) {}

    public OpenAIClientService(String apiKey, String systemPrompt) {
        this.systemPrompt = systemPrompt;
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }

    private void addMessage(ChatCompletionRole role, String content) {
        String roleStr;
        if (role.equals(SYSTEM)) {
            roleStr = "system";
        } else if (role.equals(USER)) {
            roleStr = "user";
        } else if (role.equals(ASSISTANT)) {
            roleStr = "assistant";
        } else {
            roleStr = "unknown";
        }
        conversationHistory.add(new Conversation(roleStr, content));
    }

    public Flux<ServerSentEvent<String>> sendRequisitionChatCompletion(String prompt) {
        addMessage(USER, prompt);

        // TODO: Extrair em um metodo talvez
        var mensagensParaOpenAI = conversationHistory.stream()
                .map(m -> switch (m.role()) {
                    case "system" -> ChatCompletionMessageParam.ofSystem(
                            ChatCompletionSystemMessageParam.builder().content(m.content()).build());
                    case "user" -> ChatCompletionMessageParam.ofUser(
                            ChatCompletionUserMessageParam.builder().content(m.content()).build());
                    case "assistant" -> ChatCompletionMessageParam.ofAssistant(
                            ChatCompletionAssistantMessageParam.builder().content(m.content()).build());
                    default -> throw new IllegalStateException("Role desconhecida: " + m.role());
                })
                .toList();

        var params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage(systemPrompt)
                .messages(mensagensParaOpenAI)
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
                addMessage(ASSISTANT, responseBuilder.toString());
                sink.complete();
            });

        });
    }
}
