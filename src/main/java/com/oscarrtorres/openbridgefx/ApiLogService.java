package com.oscarrtorres.openbridgefx;

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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiLogService {

    private String logFilePath;

    public ApiLogService() {
        Path logsDirPath = Paths.get("logs");

        // Check if directory exists, if not, create it
        try {
            if (Files.notExists(logsDirPath)) {
                Files.createDirectories(logsDirPath);
                System.out.println("Created 'logs/' directory.");
            }
        } catch (Exception e){}

        String name = getCurrentTimestamp().replaceAll("[^a-zA-Z0-9._-]", "_");
        this.logFilePath = "logs/" + name + ".json";
    }


    // Method to save the prompt, parameters, and output to a JSON file
    public void saveToJsonFile(String rawPrompt, String finalPrompt, Map<String, String> parameters, String output) {
        JSONArray logArray = new JSONArray();

        // Load existing log entries from the file (if the file exists)
        try {
            File logFile = new File(logFilePath);
            if (logFile.exists()) {
                String existingData = new String(Files.readAllBytes(Paths.get(logFilePath)), StandardCharsets.UTF_8);
                logArray = new JSONArray(existingData);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error (could log it or notify the user)
        }

        // Create new log entry
        JSONObject logEntry = new JSONObject();
        logEntry.put("timestamp", getCurrentTimestamp());
        logEntry.put("rawPrompt", rawPrompt);
        logEntry.put("finalPrompt", finalPrompt);
        logEntry.put("parameters", parameters);
        logEntry.put("response", output);

        // Append the new log entry to the array
        logArray.put(logEntry);

        // Write the updated log array back to the file
        try (FileWriter file = new FileWriter(logFilePath)) {
            file.write(logArray.toString(4)); // Indented JSON for readability
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Map<String, String>> loadFromJsonFile(String logFileName) {
        String logFilePath = "logs/" + logFileName;
        ObservableList<Map<String, String>> logEntries = FXCollections.observableArrayList();

        try {
            // Read the JSON file as a string
            String jsonContent = new String(Files.readAllBytes(Paths.get(logFilePath)), StandardCharsets.UTF_8);

            // Parse the string as a JSONArray
            JSONArray logArray = new JSONArray(jsonContent);

            // Iterate over the JSON array and add the entries to the ObservableList
            for (int i = 0; i < logArray.length(); i++) {
                JSONObject logEntry = logArray.getJSONObject(i);

                // Convert the logEntry JSONObject to a Map
                Map<String, String> entryMap = logEntry.toMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                logEntries.add(entryMap);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle error while reading the file
        } catch (Exception e) {
            e.printStackTrace(); // Handle any JSON parsing errors
        }

        return logEntries;
    }

    public List<String> getLogFileNames() {
        String logDirectoryPath = "logs/";
        Path logDirectory = Paths.get(logDirectoryPath);

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
            }catch (Exception e) {
                System.out.println(e);
            }
        }
        return logFileNames;
    }

    // Utility method to get the current timestamp
    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
