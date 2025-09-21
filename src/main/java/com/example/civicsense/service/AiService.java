package com.example.civicsense.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private final OpenAiService openAiService;

    public AiService(@Value("${OPENAI_API_KEY}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    public String askAi(String userMessage) {
        ChatMessage system = new ChatMessage("system", "You are Civi, a friendly civic assistant that answers questions concisely about city services and reports.");
        ChatMessage user = new ChatMessage("user", userMessage);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(system);
        messages.add(user);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4") 
                .messages(messages)
                .maxTokens(200)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        if (result.getChoices() != null && !result.getChoices().isEmpty()) {
            return result.getChoices().get(0).getMessage().getContent();
        } else {
            return "Sorry, I couldn't understand. Can you rephrase?";
        }
    }
}
