package com.oscarrtorres.openbridgefx;

import com.knuddels.jtokkit.api.ModelType;
import com.oscarrtorres.openbridgefx.models.ConversationEntry;
import com.oscarrtorres.openbridgefx.models.EnvData;
import com.oscarrtorres.openbridgefx.models.TokenCostInfo;
import com.oscarrtorres.openbridgefx.services.ApiService;
import com.oscarrtorres.openbridgefx.services.ConversationLogService;
import com.oscarrtorres.openbridgefx.services.TokenService;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    private final ConversationLogService conversationLogService = new ConversationLogService();
    private TokenService tokenService;
    private final EnvData envData = new EnvData();

    @FXML
    public void initialize() {
        checkEnvFile();

        promptTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateParameters(newValue);
        });

        outputScrollPane.vvalueProperty().bind(outputContainer.heightProperty());

        parameterScrollPane.setVisible(false);
        parameterScrollPane.setManaged(false);

        // Set items for the ComboBox
        ObservableList<String> options = FXCollections.observableArrayList(conversationLogService.getConversationFileNames());

        // Set items for the ComboBox
        conversationComboBox.setItems(options);
        conversationComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onComboBoxChange(newValue);
        });
    }

    private void checkEnvFile() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            showEnvDialog();
        }

        // file exists, but does it have all the required values?
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("API_KEY");
        String apiUrl = dotenv.get("API_URL");
        String model = dotenv.get("MODEL");
        envData.setApiKey(apiKey);
        envData.setApiUrl(apiUrl);
        envData.setModel(model);

        if (!envData.isValid()) {
            showEnvDialog();
        }

        tokenService = new TokenService(ModelType.fromName(envData.getModel()).orElseThrow());
    }

    private void showEnvDialog() {
        EnvFileDialog envFileDialog = new EnvFileDialog(this, envData);
        envFileDialog.showDialog(); // Show the dialog
    }

    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void onComboBoxChange(String selectedOption) {
        conversationLogService.setFileName(selectedOption);

        ObservableList<ConversationEntry> conversation = conversationLogService.loadFromJsonFile(selectedOption);

        clearMessageBubbles();
        for (ConversationEntry entry : conversation) {
            addMessageBubble(entry, true);
            addMessageBubble(entry, false);
        }
    }

    private void updateParameters(String prompt) {
        Map<String, String> currentParameters = new HashMap<>();

        // Collect current parameters from the UI
        for (var node : parameterContainer.getChildren()) {
            if (node instanceof HBox parameterSet) {
                TextField keyField = (TextField) parameterSet.getChildren().get(1);
                TextField valueField = (TextField) parameterSet.getChildren().get(3);

                String key = keyField.getText().trim();
                String value = valueField.getText().trim();

                if (!key.isEmpty()) {
                    currentParameters.put(key, value);
                }
            }
        }

        // Clear existing parameter fields in the UI
        parameterContainer.getChildren().clear();

        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(prompt);

        Set<String> uniqueKeys = new HashSet<>();
        boolean hasParameters = false;

        while (matcher.find()) {
            String key = matcher.group(1);
            if (uniqueKeys.add(key)) {
                // Check if this key already has a value
                String value = currentParameters.getOrDefault(key, ""); // Use existing value if present
                addParameterField(key, value);
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
        addParameterField(key, "");
    }

    public void addParameterField(String key, String value) {
        HBox parameterSet = new HBox(10);
        Label keyLabel = new Label("Key:");
        TextField keyField = new TextField();
        keyField.setText(key);
        keyField.setPromptText("Enter your param key here...");
        Label valueLabel = new Label("Value:");
        TextField valueField = new TextField();
        valueField.setText(value);
        valueField.setPromptText("Enter your param value here...");

        HBox.setHgrow(valueField, Priority.ALWAYS);

        parameterSet.getChildren().addAll(keyLabel, keyField, valueLabel, valueField);
        parameterContainer.getChildren().add(parameterSet);
    }

    @FXML
    public void onSendButtonClick() {
        ConversationEntry conversationEntry = new ConversationEntry();
        conversationEntry.setTimestamp(conversationLogService.getCurrentTimestamp());

        conversationEntry.setRawPrompt(promptTextArea.getText());
        Map<String, String> parameters = new HashMap<>();

        // Collect parameter key-value pairs from the parameter container
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
        conversationEntry.setParameters(parameters);

        // Replace placeholders in prompt with parameter values
        String parsedPrompt = conversationEntry.getRawPrompt();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            parsedPrompt = parsedPrompt.replace(placeholder, entry.getValue());
        }

        conversationEntry.setFinalPrompt(parsedPrompt);
        conversationEntry.setPromptInfo(tokenService.getPromptInfo(parsedPrompt));

        addMessageBubble(conversationEntry, true);

        // Create and start the GPT API service
        ApiService gptApiService = new ApiService(conversationEntry.getFinalPrompt(), envData);

        gptApiService.setOnSucceeded(event -> {
            String gptResponse = gptApiService.getValue();
            conversationEntry.setResponse(gptResponse);
            conversationEntry.setResponseInfo(tokenService.getResponseInfo(gptResponse));

            addMessageBubble(conversationEntry, false); // Add GPT response as received message

            // Create a LogEntry object with the necessary details
            // Save the log entry using the updated saveEntryToFile method
            conversationLogService.saveEntryToFile(conversationEntry);
        });

        gptApiService.setOnFailed(event -> {
            Throwable exception = gptApiService.getException();
            conversationEntry.setResponse("Error: " + exception.getMessage());
            addMessageBubble(conversationEntry, false); // Handle error
        });

        gptApiService.start(); // Start the service
    }

    private void addMessageBubble(ConversationEntry entry, boolean isSent) {
        String message = isSent ? entry.getFinalPrompt() : entry.getResponse();
        String timestamp = entry.getTimestamp();

        // Using TextFlow for better text wrapping and dynamic height adjustment
        TextFlow messageTextFlow = new TextFlow(new Text(message));
        messageTextFlow.setMaxWidth(500);  // Set a maximum width for wrapping
        messageTextFlow.setPadding(new Insets(10));
        messageTextFlow.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-background-radius: 15;");

        // Styling the text background based on sent/received
        if (isSent) {
            messageTextFlow.setStyle(messageTextFlow.getStyle() + "-fx-background-color: lightblue; -fx-text-fill: black;");
        } else {
            messageTextFlow.setStyle(messageTextFlow.getStyle() + "-fx-background-color: lightgreen; -fx-text-fill: black;");
        }

        // Create sender label with timestamp at the top
        Label senderLabel = new Label(isSent ? "You (" + timestamp + ")" : "Other (" + timestamp + ")");
        senderLabel.setTextFill(Color.GRAY);
        senderLabel.setFont(new Font("Arial", 12));

        // Bottom label (e.g., token info or any other text)
        TokenCostInfo tokenCostInfo = isSent ? entry.getPromptInfo() : entry.getResponseInfo();
        Label bottomLabel = new Label(tokenCostInfo.toString());  // Replace with actual token info
        bottomLabel.setTextFill(Color.GRAY);
        bottomLabel.setFont(new Font("Arial", 12));

        VBox bubbleContainer = new VBox(5);
        bubbleContainer.getChildren().addAll(senderLabel, messageTextFlow, bottomLabel);

        HBox messageBubble = new HBox();
        messageBubble.getChildren().add(bubbleContainer);

        if (isSent) {
            messageBubble.setAlignment(Pos.CENTER_RIGHT);
            messageBubble.setUserData(entry);
            messageBubble.setCursor(javafx.scene.Cursor.HAND);
            messageBubble.setOnMouseClicked(event -> onMessageBubbleClick(messageBubble));
        } else {
            messageBubble.setAlignment(Pos.CENTER_LEFT);
        }

        // Ensure dynamic height adjustments are allowed
        messageBubble.setPrefHeight(Region.USE_COMPUTED_SIZE);
        bubbleContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        outputContainer.getChildren().add(messageBubble);
    }

    private void clearMessageBubbles() {
        outputContainer.getChildren().clear();
    }

    private void onMessageBubbleClick(HBox messageBubble) {
        ConversationEntry data = (ConversationEntry) messageBubble.getUserData();

        promptTextArea.setText(data.getRawPrompt()); // will trigger updateParameters()

        for (var node : parameterContainer.getChildren()) {
            if (node instanceof HBox parameterSet) {
                TextField keyField = (TextField) parameterSet.getChildren().get(1);
                TextField valueField = (TextField) parameterSet.getChildren().get(3);

                String key = keyField.getText().trim();

                valueField.setText(data.getParameters().getOrDefault(key, ""));
            }
        }

        parameterScrollPane.setVisible(!data.getParameters().isEmpty());
        parameterScrollPane.setManaged(!data.getParameters().isEmpty());
    }
}
