package com.oscarrtorres.openbridgefx.services;

import com.oscarrtorres.openbridgefx.models.ChatData;
import com.oscarrtorres.openbridgefx.models.ChatEntry;
import com.oscarrtorres.openbridgefx.models.Constants;
import com.oscarrtorres.openbridgefx.models.TokenCostInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ChatService {

    private ChatData currentChatData;

    public ChatService() {
        Path logsDirPath = Paths.get(Constants.CHATS_DIR_PATH);

        // Check if directory exists, if not, create it
        try {
            if (Files.notExists(logsDirPath)) {
                Files.createDirectories(logsDirPath);
                System.out.println("Created '" + Constants.CHATS_DIR_PATH + "' directory.");
            }

            String fileName = getCurrentTimestamp().replaceAll("[^a-zA-Z0-9._-]", "_") + ".json";
            this.currentChatData = new ChatData();
            this.currentChatData.setFileName(fileName);
            this.currentChatData.setChatEntries(FXCollections.observableArrayList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCurrentChatData(ChatData chatData) {
        this.currentChatData = chatData;
    }

    public ChatData getCurrentChatData() {
        return currentChatData;
    }

    public void saveChatData() {
        String filePath = Constants.CHATS_DIR_PATH + File.separator + this.currentChatData.getFileName();
        JSONArray jsonArray = new JSONArray();
        ObservableList<ChatEntry> chatEntries = this.currentChatData.getChatEntries();

        for (ChatEntry entry : chatEntries) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("timestamp", entry.getTimestamp());
            jsonObject.put("rawPrompt", entry.getRawPrompt());
            jsonObject.put("finalPrompt", entry.getFinalPrompt());
            jsonObject.put("response", entry.getResponse());

            // Convert parameters to JSONObject
            JSONObject parametersJson = new JSONObject(entry.getParameters());
            jsonObject.put("parameters", parametersJson);

            // Add token cost info if present
            if (entry.getPromptInfo() != null) {
                JSONObject promptTokenJson = new JSONObject();
                promptTokenJson.put("tokenCount", entry.getPromptInfo().getTokenCount());
                promptTokenJson.put("totalCost", entry.getPromptInfo().getTotalCost());
                jsonObject.put("promptTokenInfo", promptTokenJson);
            }

            if (entry.getResponseInfo() != null) {
                JSONObject responseTokenJson = new JSONObject();
                responseTokenJson.put("tokenCount", entry.getResponseInfo().getTokenCount());
                responseTokenJson.put("totalCost", entry.getResponseInfo().getTotalCost());
                jsonObject.put("responseTokenInfo", responseTokenJson);
            }

            // Add the JSON object to the JSON array
            jsonArray.put(jsonObject);
        }

        // Write the updated log array back to the file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonArray.toString(4)); // Indented JSON for readability
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ChatData loadFromJsonFile(String logFileName) {
        String logFilePath = Constants.CHATS_DIR_PATH + File.separator + logFileName;
        ObservableList<ChatEntry> logEntries = FXCollections.observableArrayList();

        try {
            // Read the JSON file as a string
            String jsonContent = new String(Files.readAllBytes(Paths.get(logFilePath)), StandardCharsets.UTF_8);

            // Parse the string as a JSONArray
            JSONArray logArray = new JSONArray(jsonContent);

            // Iterate over the JSON array and add the entries as ConversationEntry objects
            for (int i = 0; i < logArray.length(); i++) {
                JSONObject logEntryJson = logArray.getJSONObject(i);

                // Extract parameters as a Map
                Map<String, String> parameters = new HashMap<>();
                JSONObject parametersJson = logEntryJson.getJSONObject("parameters");
                for (String key : parametersJson.keySet()) {
                    parameters.put(key, parametersJson.getString(key));
                }

                // Parse promptTokenInfo and responseTokenInfo
                TokenCostInfo promptTokenInfo = null;
                TokenCostInfo responseTokenInfo = null;

                if (logEntryJson.has("promptTokenInfo")) {
                    JSONObject promptTokenJson = logEntryJson.getJSONObject("promptTokenInfo");
                    int promptTokenCount = promptTokenJson.optInt("tokenCount", 0);
                    double promptTotalCost = promptTokenJson.optDouble("totalCost", 0.0);
                    promptTokenInfo = new TokenCostInfo(promptTokenCount, promptTotalCost);
                }

                if (logEntryJson.has("responseTokenInfo")) {
                    JSONObject responseTokenJson = logEntryJson.getJSONObject("responseTokenInfo");
                    int responseTokenCount = responseTokenJson.optInt("tokenCount", 0);
                    double responseTotalCost = responseTokenJson.optDouble("totalCost", 0.0);
                    responseTokenInfo = new TokenCostInfo(responseTokenCount, responseTotalCost);
                }

                // Create a ConversationEntry object from JSON
                ChatEntry chatEntry = new ChatEntry(
                        logEntryJson.optString("timestamp"),
                        logEntryJson.optString("rawPrompt"),
                        logEntryJson.optString("response"),
                        logEntryJson.optString("finalPrompt"),
                        parameters,
                        true,
                        promptTokenInfo,
                        responseTokenInfo
                );

                logEntries.add(chatEntry);
            }


        } catch (IOException e) {
            e.printStackTrace(); // Handle error while reading the file
        } catch (Exception e) {
            e.printStackTrace(); // Handle any JSON parsing errors
        }

        // set ChatData
        ChatData chatData = new ChatData();
        chatData.setFileName(logFileName);
        chatData.setChatEntries(logEntries);
        chatData.setTotalCharge(logEntries.stream()
                .mapToDouble(e -> e.getPromptInfo().getTotalCost() + e.getResponseInfo().getTotalCost()) // Extract the cost as a double stream
                .sum());
        chatData.setTimestamp(logEntries.get(logEntries.size()-1).getTimestamp());

        return chatData;
    }


    public List<ChatData> getChatDataFromFiles() {
        Path logDirectory = Paths.get(Constants.CHATS_DIR_PATH);

        List<ChatData> chatDataList = new ArrayList<>();
        // Check if the directory exists and is a directory
        if (Files.exists(logDirectory) && Files.isDirectory(logDirectory)) {
            try {

                chatDataList = Files.list(logDirectory)
                        .filter(path -> path.toString().endsWith(".json"))
                        .sorted(Comparator.comparingLong(path -> {
                            try {
                                return Files.getLastModifiedTime((Path) path).toMillis();
                            } catch (Exception e) {
                                // Handle exception, e.g., log or return a default value
                                return 0; // or some other fallback value
                            }
                        }).reversed()) // Reverse to get latest first
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .map(this::loadFromJsonFile)
                        .collect(Collectors.toCollection(ArrayList::new));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return chatDataList;
    }

    // Utility method to get the current timestamp
    public String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
