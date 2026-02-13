package com.shabin.aistudysummarizer.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateSummary(String text) {
        String prompt = "You are an AI study assistant. Analyze the following text and generate a structured study summary in JSON format. "
                +
                "The response MUST be a valid JSON object with the following structure: " +
                "{" +
                "  \"executiveSummary\": \"A concise overview of the main topics.\", " +
                "  \"sectionSummary\": [\"Key point 1\", \"Key point 2\"], " +
                "  \"keyTerms\": [{\"term\": \"Term name\", \"definition\": \"Definition\"}], " +
                "  \"mcqs\": [{\"question\": \"Question?\", \"options\": [\"O1\", \"O2\", \"O3\", \"O4\"], \"answer\": \"Correct Option\", \"explanation\": \"Why?\"}], "
                +
                "  \"flashcards\": [{\"front\": \"Question/Term\", \"back\": \"Answer/Explanation\"}], " +
                "  \"examInsights\": [\"Likely exam topics\"]" +
                "}\n\n" +
                "Text to analyze:\n" +
                text;

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", "gpt-3.5-turbo-0125"); // or gpt-4-turbo-preview for better results
        jsonBody.put("response_format", new JSONObject().put("type", "json_object"));

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        jsonBody.put("messages", new org.json.JSONArray().put(message));

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("OpenAI API error: {} - {}", response.code(), errorBody);
                throw new RuntimeException("OpenAI API call failed: " + response.message());
            }

            String responseData = response.body().string();
            JSONObject responseObject = new JSONObject(responseData);
            return responseObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (IOException e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to communicate with OpenAI: " + e.getMessage());
        }
    }
}
