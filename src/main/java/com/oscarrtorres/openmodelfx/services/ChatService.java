package com.oscarrtorres.openmodelfx.services;

import com.oscarrtorres.openmodelfx.models.ChatData;
import com.oscarrtorres.openmodelfx.models.ChatEntry;
import com.oscarrtorres.openmodelfx.models.Constants;
import com.oscarrtorres.openmodelfx.models.TokenCostInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Data
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

            this.currentChatData = new ChatData(getNewFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            jsonObject.put("modelName", entry.getModelName());
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

    private ChatData loadFromJsonFile(String fileName) {
        String chatFilePath = Constants.CHATS_DIR_PATH + File.separator + fileName;
        ObservableList<ChatEntry> chatEntries = FXCollections.observableArrayList();

        try {
            // Read the JSON file as a string
            String chatContent = Files.readString(Paths.get(chatFilePath));

            // Parse the string as a JSONArray
            JSONArray chatDataArray = new JSONArray(chatContent);

            // Iterate over the JSON array and add the entries as ConversationEntry objects
            for (int i = 0; i < chatDataArray.length(); i++) {
                JSONObject chatEntryJson = chatDataArray.getJSONObject(i);

                // Extract parameters as a Map
                Map<String, String> parameters = new HashMap<>();
                JSONObject parametersJson = chatEntryJson.getJSONObject("parameters");
                for (String key : parametersJson.keySet()) {
                    parameters.put(key, parametersJson.getString(key));
                }

                // Parse promptTokenInfo and responseTokenInfo
                TokenCostInfo promptTokenInfo = null;
                TokenCostInfo responseTokenInfo = null;

                if (chatEntryJson.has("promptTokenInfo")) {
                    JSONObject promptTokenJson = chatEntryJson.getJSONObject("promptTokenInfo");
                    int promptTokenCount = promptTokenJson.optInt("tokenCount", 0);
                    double promptTotalCost = promptTokenJson.optDouble("totalCost", 0.0);
                    promptTokenInfo = new TokenCostInfo(promptTokenCount, promptTotalCost);
                }

                if (chatEntryJson.has("responseTokenInfo")) {
                    JSONObject responseTokenJson = chatEntryJson.getJSONObject("responseTokenInfo");
                    int responseTokenCount = responseTokenJson.optInt("tokenCount", 0);
                    double responseTotalCost = responseTokenJson.optDouble("totalCost", 0.0);
                    responseTokenInfo = new TokenCostInfo(responseTokenCount, responseTotalCost);
                }

                // Create a ConversationEntry object from JSON
                ChatEntry chatEntry = new ChatEntry(
                        chatEntryJson.optString("timestamp"),
                        chatEntryJson.optString("rawPrompt"),
                        chatEntryJson.optString("response"),
                        chatEntryJson.optString("finalPrompt"),
                        chatEntryJson.optString("modelName"),
                        parameters,
                        true,
                        promptTokenInfo,
                        responseTokenInfo
                );

                chatEntries.add(chatEntry);
            }


        } catch (IOException e) {
            e.printStackTrace(); // Handle error while reading the file
        } catch (Exception e) {
            e.printStackTrace(); // Handle any JSON parsing errors
        }

        // set ChatData
        ChatData chatData = new ChatData();
        chatData.setFileName(fileName);
        chatData.setChatEntries(chatEntries);
        chatData.setTotalCharge(chatEntries.stream()
                .mapToDouble(e -> e.getPromptInfo().getTotalCost() + e.getResponseInfo().getTotalCost()) // Extract the cost as a double stream
                .sum());
        chatData.setTimestamp(chatEntries.get(chatEntries.size()-1).getTimestamp());

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

    public static String getNewFileName() {
        return getCurrentTimestamp().replaceAll("[^a-zA-Z0-9._-]", "_") + ".json";
    }

    // Utility method to get the current timestamp
    public static String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
