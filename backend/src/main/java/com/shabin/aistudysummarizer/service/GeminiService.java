package com.shabin.aistudysummarizer.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1/models/";

    public String generateSummary(String text, int mcqCount) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured. Please set GEMINI_API_KEY in your .env file.");
        }

        String prompt = "You are an AI study assistant. Analyze the following text and generate a structured study summary in JSON format. "
                + "The response MUST be a valid JSON object with the following structure: "
                + "{ \"executiveSummary\": \"A concise overview of the main topics.\", "
                + "\"sectionSummary\": [\"Key point 1\", \"Key point 2\"], "
                + "\"keyTerms\": [{\"term\": \"Term name\", \"definition\": \"Definition\"}], "
                + "\"mcqs\": [{\"question\": \"Question?\", \"options\": [\"O1\", \"O2\", \"O3\", \"O4\"], \"answer\": \"Correct Option\", \"explanation\": \"Why?\"}], "
                + "\"flashcards\": [{\"front\": \"Question/Term\", \"back\": \"Answer/Explanation\"}], "
                + "\"examInsights\": [\"Likely exam topics\"] }\n\n"
                + "IMPORTANT FLASHCARD GUIDELINES:\n"
                + "- Generate comprehensive flashcards covering ALL major topics and sub-topics in the text\n"
                + "- Include flashcards for: main concepts, definitions, relationships between topics, examples, and key facts\n"
                + "- Provide detailed explanations on the back side (100-200 words where applicable)\n"
                + "- Create flashcards that progress from basic definitions to complex relationships\n"
                + "- Ensure each flashcard is self-contained and can be studied independently\n"
                + "- Generate AT LEAST 15-20 flashcards (more for longer/complex documents)\n\n"
                + "IMPORTANT: Generate EXACTLY " + mcqCount + " multiple choice questions in the mcqs array. No more, no less.\n\n"
                + "Text to analyze:\n" + text;

        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject requestContent = new JSONObject().put("parts", new JSONArray().put(part));

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);

        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", new JSONArray().put(requestContent));
        requestBody.put("generationConfig", generationConfig);

        String url = API_BASE + model + ":generateContent?key=" + apiKey;
        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseData = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("Gemini API error: {} - {}", response.code(), responseData);
                String errorMessage = parseGeminiError(responseData, response.code());
                throw new RuntimeException("Gemini API call failed: " + errorMessage);
            }

            JSONObject json;
            try {
                json = new JSONObject(responseData);
            } catch (org.json.JSONException e) {
                log.error("Invalid Gemini response JSON: {}", responseData.length() > 500 ? responseData.substring(0, 500) + "..." : responseData);
                throw new RuntimeException("Invalid response from Gemini API. Please try again.");
            }
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Gemini API returned no response. The content may have been blocked.");
            }

            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject responseContent = candidate.optJSONObject("content");
            if (responseContent == null) {
                String finishReason = candidate.optString("finishReason", "UNKNOWN");
                throw new RuntimeException("Gemini blocked the response. Finish reason: " + finishReason);
            }

            JSONArray parts = responseContent.optJSONArray("parts");
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("Gemini API returned empty content.");
            }

            String responseText = parts.getJSONObject(0).optString("text", "");
            if (responseText.isBlank()) {
                throw new RuntimeException("Gemini API returned empty text.");
            }
            return extractJsonFromResponse(responseText);

        } catch (org.json.JSONException e) {
            log.error("Error parsing Gemini response", e);
            throw new RuntimeException("Invalid response from Gemini: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to communicate with Gemini: " + e.getMessage());
        }
    }

    public String generateMoreMcqs(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured. Please set GEMINI_API_KEY in your .env file.");
        }

        String prompt = "You are an AI study assistant. Generate 3-5 additional multiple choice questions based on the following text. "
                + "Return ONLY a valid JSON array with this structure: "
                + "[{\"question\": \"Question?\", \"options\": [\"O1\", \"O2\", \"O3\", \"O4\"], \"answer\": \"Correct Option\", \"explanation\": \"Why?\"}] "
                + "Do not include any markdown, code blocks, or extra text. Only the JSON array.\n\n"
                + "Text to analyze:\n" + text;

        return callGeminiApiForContent(prompt, "MCQs");
    }

    public String generateMoreFlashcards(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured. Please set GEMINI_API_KEY in your .env file.");
        }

        String prompt = "You are an AI study assistant. Generate 8-12 additional comprehensive flashcards based on the following text. "
                + "Focus on topics and sub-topics that haven't been heavily covered. "
                + "Return ONLY a valid JSON array with this structure: "
                + "[{\"front\": \"Question/Term\", \"back\": \"Detailed Answer/Explanation (100-200 words)\"}] "
                + "Guidelines:\n"
                + "- Include definitions, examples, relationships, and practical applications\n"
                + "- Cover advanced concepts and edge cases\n"
                + "- Provide context and explanations, not just facts\n"
                + "- Make answers detailed enough to be fully educational\n"
                + "Do not include any markdown, code blocks, or extra text. Only the JSON array.\n\n"
                + "Text to analyze:\n" + text;

        return callGeminiApiForContent(prompt, "Flashcards");
    }

    public String generateMoreSummary(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured. Please set GEMINI_API_KEY in your .env file.");
        }

        String prompt = "You are an AI study assistant. Generate an alternative executive summary of the following text, "
                + "emphasizing different aspects than a standard overview. "
                + "Return ONLY a plain text summary (no JSON, no markdown). Just the summary text.\n\n"
                + "Text to analyze:\n" + text;

        return callGeminiApiForContent(prompt, "Summary");
    }

    private String callGeminiApiForContent(String prompt, String contentType) {
        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject requestContent = new JSONObject().put("parts", new JSONArray().put(part));

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.8);

        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", new JSONArray().put(requestContent));
        requestBody.put("generationConfig", generationConfig);

        String url = API_BASE + model + ":generateContent?key=" + apiKey;
        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseData = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("Gemini API error: {} - {}", response.code(), responseData);
                String errorMessage = parseGeminiError(responseData, response.code());
                throw new RuntimeException("Gemini API call failed: " + errorMessage);
            }

            JSONObject json;
            try {
                json = new JSONObject(responseData);
            } catch (org.json.JSONException e) {
                log.error("Invalid Gemini response JSON: {}", responseData.length() > 500 ? responseData.substring(0, 500) + "..." : responseData);
                throw new RuntimeException("Invalid response from Gemini API. Please try again.");
            }
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Gemini API returned no response. The content may have been blocked.");
            }

            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject responseContent = candidate.optJSONObject("content");
            if (responseContent == null) {
                String finishReason = candidate.optString("finishReason", "UNKNOWN");
                throw new RuntimeException("Gemini blocked the response. Finish reason: " + finishReason);
            }

            JSONArray parts = responseContent.optJSONArray("parts");
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("Gemini API returned empty content.");
            }

            String responseText = parts.getJSONObject(0).optString("text", "");
            if (responseText.isBlank()) {
                throw new RuntimeException("Gemini API returned empty text.");
            }
            return contentType.equals("Summary") ? responseText : extractJsonFromResponse(responseText);

        } catch (org.json.JSONException e) {
            log.error("Error parsing Gemini response", e);
            throw new RuntimeException("Invalid response from Gemini: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to communicate with Gemini: " + e.getMessage());
        }
    }

    public List<String> listAvailableModels() {
        if (apiKey == null || apiKey.isBlank()) {
            return List.of("(Set GEMINI_API_KEY to list models)");
        }
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = client.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "{}";
                JSONObject json = new JSONObject(body);
                JSONArray models = json.optJSONArray("models");
                if (models == null) return List.of("(No models in response)");
                List<String> names = new java.util.ArrayList<>();
                for (int i = 0; i < models.length(); i++) {
                    String name = models.getJSONObject(i).optString("name", "").replace("models/", "");
                    if (!name.isEmpty()) names.add(name);
                }
                return names;
            }
        } catch (Exception e) {
            log.warn("Could not list models: {}", e.getMessage());
            return List.of("(Error: " + e.getMessage() + ")");
        }
    }

    private String extractJsonFromResponse(String text) {
        if (text == null || text.isBlank()) return text;
        text = text.trim();
        if (text.startsWith("```")) {
            int start = text.indexOf("\n") + 1;
            int end = text.lastIndexOf("```");
            if (end > start) {
                text = text.substring(start, end).trim();
            }
        }
        // Parse the JSON string to ensure proper escaping of newlines
        try {
            JSONObject json = new JSONObject(text);
            return json.toString();
        } catch (org.json.JSONException e) {
            log.warn("Could not parse extracted JSON, returning as-is: {}", e.getMessage());
            return text;
        }
    }

    private String parseGeminiError(String errorBody, int code) {
        if (errorBody == null || errorBody.isBlank()) {
            return "HTTP " + code + " - Check your API key and try again.";
        }
        try {
            JSONObject json = new JSONObject(errorBody);
            if (json.has("error") && json.get("error") instanceof JSONObject) {
                JSONObject error = json.getJSONObject("error");
                if (error.has("message")) {
                    return error.getString("message");
                }
            }
        } catch (Exception ignored) {
            // Fall through
        }
        return errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
    }
}
