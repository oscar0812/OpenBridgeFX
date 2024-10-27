package com.oscarrtorres.openbridgefx.services;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiService extends Service<String> {

    private final String apiUrl;
    private final String apiKey;
    private final String prompt;

    public ApiService(String prompt) {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("API_KEY");
        this.apiUrl = dotenv.get("API_URL");
        this.prompt = prompt;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create a JSON object for the request
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "gpt-4o-mini");

                // Create the messages array
                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);
                messages.put(message);

                requestBody.put("messages", messages);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                String response;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder responseBuilder = new StringBuilder();
                    try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                    response = parseGptResponse(responseBuilder.toString());
                } else {
                    response = "Error: " + responseCode;
                }

                // Save input, parameters, and output to a JSON log file
                return response;
            }
        };
    }

    private String parseGptResponse(String jsonResponse) {
        // Parse the JSON response
        JSONObject jsonObject = new JSONObject(jsonResponse);

        // Get the "choices" array from the JSON object
        JSONArray choices = jsonObject.getJSONArray("choices");

        // Extract the content from the first choice
        String messageContent = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        return messageContent; // Return the extracted content
    }

}

