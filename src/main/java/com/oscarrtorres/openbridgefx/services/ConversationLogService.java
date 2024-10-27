package com.oscarrtorres.openbridgefx.services;

import com.oscarrtorres.openbridgefx.models.ConversationEntry;
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

public class ConversationLogService {

    private String logFilePath;
    private static final String SUB_PATH = "conversations/";

    public ConversationLogService() {
        Path logsDirPath = Paths.get(SUB_PATH);

        // Check if directory exists, if not, create it
        try {
            if (Files.notExists(logsDirPath)) {
                Files.createDirectories(logsDirPath);
                System.out.println("Created '" + SUB_PATH + "' directory.");
            }

            String fileName = getCurrentTimestamp().replaceAll("[^a-zA-Z0-9._-]", "_");
            this.setFileName(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        if (!fileName.endsWith(".json")) {
            this.logFilePath = SUB_PATH + fileName + ".json";
        } else {
            this.logFilePath = SUB_PATH + fileName;
        }
    }


    public void saveEntryToFile(ConversationEntry conversationEntry) {
        JSONArray logArray = new JSONArray();

        // Load existing log entries from the file (if the file exists)
        try {
            File logFile = new File(logFilePath);
            if (logFile.exists()) {
                String existingData = Files.readString(Paths.get(logFilePath));
                logArray = new JSONArray(existingData);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error (could log it or notify the user)
        }

        // Create new JSON object from LogEntry
        JSONObject logEntryJson = new JSONObject();
        logEntryJson.put("timestamp", conversationEntry.getTimestamp());
        logEntryJson.put("rawPrompt", conversationEntry.getRawPrompt());
        logEntryJson.put("finalPrompt", conversationEntry.getFinalPrompt());
        logEntryJson.put("parameters", conversationEntry.getParameters());
        logEntryJson.put("response", conversationEntry.getResponse());

        // Convert promptTokenInfo to JSON
        if (conversationEntry.getPromptInfo() != null) {
            JSONObject promptTokenJson = new JSONObject();
            promptTokenJson.put("tokenCount", conversationEntry.getPromptInfo().getTokenCount());
            promptTokenJson.put("totalCost", conversationEntry.getPromptInfo().getTotalCost());
            logEntryJson.put("promptTokenInfo", promptTokenJson);
        }

        // Convert responseTokenInfo to JSON
        if (conversationEntry.getResponseInfo() != null) {
            JSONObject responseTokenJson = new JSONObject();
            responseTokenJson.put("tokenCount", conversationEntry.getResponseInfo().getTokenCount());
            responseTokenJson.put("totalCost", conversationEntry.getResponseInfo().getTotalCost());
            logEntryJson.put("responseTokenInfo", responseTokenJson);
        }

        // Append the new log entry to the array
        logArray.put(logEntryJson);

        // Write the updated log array back to the file
        try (FileWriter file = new FileWriter(logFilePath)) {
            file.write(logArray.toString(4)); // Indented JSON for readability
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<ConversationEntry> loadFromJsonFile(String logFileName) {
        String logFilePath = SUB_PATH + logFileName;
        ObservableList<ConversationEntry> logEntries = FXCollections.observableArrayList();

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
                ConversationEntry conversationEntry = new ConversationEntry(
                        logEntryJson.optString("timestamp"),
                        logEntryJson.optString("rawPrompt"),
                        logEntryJson.optString("response"),
                        logEntryJson.optString("finalPrompt"),
                        parameters,
                        true,
                        promptTokenInfo,
                        responseTokenInfo
                );

                logEntries.add(conversationEntry);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle error while reading the file
        } catch (Exception e) {
            e.printStackTrace(); // Handle any JSON parsing errors
        }

        return logEntries;
    }


    public List<String> getConversationFileNames() {
        Path logDirectory = Paths.get(SUB_PATH);

        List<String> logFileNames = new LinkedList<>();

        // Check if the directory exists and is a directory
        if (Files.exists(logDirectory) && Files.isDirectory(logDirectory)) {
            try {

                logFileNames = Files.list(logDirectory)
                        .filter(path -> path.toString().endsWith(".json"))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());

                System.out.println(logFileNames);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return logFileNames;
    }

    // Utility method to get the current timestamp
    public String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
