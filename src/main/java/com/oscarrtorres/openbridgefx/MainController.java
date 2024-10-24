package com.oscarrtorres.openbridgefx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    @FXML
    private VBox outputContainer;
    @FXML
    private ComboBox<String> conversationComboBox;
    @FXML
    private VBox outputVbox;
    @FXML
    private ScrollPane outputScrollPane;
    @FXML
    private TextArea promptTextArea;
    @FXML
    private VBox parameterContainer;
    @FXML
    private ScrollPane parameterScrollPane;

    private static final double PARAMETER_HEIGHT = 60.0;
    private static final double MAX_SCROLLPANE_HEIGHT = 300.0;

    private ApiLogService apiLogService = new ApiLogService();

    @FXML
    public void initialize() {
        promptTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateParameters(newValue);
        });

        outputScrollPane.vvalueProperty().bind(outputContainer.heightProperty());

        parameterScrollPane.setVisible(false);
        parameterScrollPane.setManaged(false);

        // Set items for the ComboBox
        ObservableList<String> options = FXCollections.observableArrayList(apiLogService.getLogFileNames());

        // Set items for the ComboBox
        conversationComboBox.setItems(options);
        conversationComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onComboBoxChange(newValue);
        });
    }

    private void onComboBoxChange(String selectedOption) {
        System.out.println("Selected: " + selectedOption);
        ObservableList<Map<String, String>> conversation = apiLogService.loadFromJsonFile(selectedOption);

        clearMessageBubbles();
        for (Map<String, String> item : conversation) {
            String timestamp = item.get("timestamp");
            addMessageBubble(item.get("rawPrompt"), true, timestamp);
            addMessageBubble(item.get("response"), false, timestamp);
        }
    }

    private void updateParameters(String prompt) {
        parameterContainer.getChildren().clear();

        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(prompt);

        Set<String> uniqueKeys = new HashSet<>();
        boolean hasParameters = false;

        while (matcher.find()) {
            String key = matcher.group(1);
            if (uniqueKeys.add(key)) {
                addParameterField(key);
                hasParameters = true;
            }
        }

        parameterScrollPane.setVisible(hasParameters);
        parameterScrollPane.setManaged(hasParameters);

        if (hasParameters) {
            int paramCount = uniqueKeys.size();
            double newHeight = Math.min(paramCount * PARAMETER_HEIGHT, MAX_SCROLLPANE_HEIGHT);
            parameterScrollPane.setPrefHeight(newHeight);
            parameterScrollPane.setMaxHeight(MAX_SCROLLPANE_HEIGHT);
        } else {
            parameterScrollPane.setPrefHeight(0);
            parameterScrollPane.setMaxHeight(0);
        }
    }

    @FXML
    public void addParameterField(String key) {
        HBox parameterSet = new HBox(10);
        Label keyLabel = new Label("Key:");
        TextField keyField = new TextField();
        keyField.setText(key);
        keyField.setPromptText("Enter your param key here...");
        Label valueLabel = new Label("Value:");
        TextField valueField = new TextField();
        valueField.setPromptText("Enter your param value here...");

        parameterSet.getChildren().addAll(keyLabel, keyField, valueLabel, valueField);
        parameterContainer.getChildren().add(parameterSet);
    }

    @FXML
    public void onSendButtonClick() {
        String prompt = promptTextArea.getText();
        Map<String, String> parameters = new HashMap<>();

        for (var node : parameterContainer.getChildren()) {
            if (node instanceof HBox parameterSet) {
                TextField keyField = (TextField) parameterSet.getChildren().get(1);
                TextField valueField = (TextField) parameterSet.getChildren().get(3);

                String key = keyField.getText().trim();
                String value = valueField.getText().trim();

                if (!key.isEmpty()) {
                    parameters.put(key, value);
                }
            }
        }

        String parsedPrompt = prompt;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            parsedPrompt = parsedPrompt.replace(placeholder, entry.getValue());
        }

        final String finalPrompt = parsedPrompt;
        addMessageBubble(finalPrompt, true); // Display sent message

        // Create and start the GPT API service
        ApiService gptApiService = new ApiService(finalPrompt);

        gptApiService.setOnSucceeded(event -> {
            String gptResponse = gptApiService.getValue();
            addMessageBubble(gptResponse, false); // Add GPT response as received message
            apiLogService.saveToJsonFile(prompt, finalPrompt, parameters, gptResponse);
        });

        gptApiService.setOnFailed(event -> {
            Throwable exception = gptApiService.getException();
            addMessageBubble("Error: " + exception.getMessage(), false); // Handle error
        });

        gptApiService.start(); // Start the service
    }

    private void addMessageBubble(String message, boolean isSent) {
        this.addMessageBubble(message, isSent, null);
    }

    private void addMessageBubble(String message, boolean isSent, String timestamp) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setPadding(new Insets(10));
        messageLabel.setFont(new Font("Arial", 14));

        // Get the current time and format it
        if(Objects.isNull(timestamp)) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a");
            timestamp = now.format(formatter);
        }

        // Create sender label with timestamp
        Label senderLabel = new Label(isSent ? "You (" + timestamp + ")" : "Other (" + timestamp + ")");
        senderLabel.setTextFill(Color.GRAY);
        senderLabel.setFont(new Font("Arial", 12));

        VBox bubbleContainer = new VBox(5);
        bubbleContainer.getChildren().addAll(senderLabel, messageLabel);

        if (isSent) {
            messageLabel.setStyle("-fx-background-color: lightblue; -fx-background-radius: 15; -fx-text-fill: black;");
        } else {
            messageLabel.setStyle("-fx-background-color: lightgreen; -fx-background-radius: 15; -fx-text-fill: black;");
        }

        HBox messageBubble = new HBox();
        messageBubble.getChildren().add(bubbleContainer);

        if (isSent) {
            messageBubble.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBubble.setAlignment(Pos.CENTER_LEFT);
        }

        outputContainer.getChildren().add(messageBubble);
    }

    private void clearMessageBubbles() {
        outputContainer.getChildren().clear();
    }
}
