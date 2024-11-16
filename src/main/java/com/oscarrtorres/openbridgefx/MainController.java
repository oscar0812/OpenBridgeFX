package com.oscarrtorres.openbridgefx;

import com.knuddels.jtokkit.api.ModelType;
import com.oscarrtorres.openbridgefx.dialogs.ApiPropertiesDialog;
import com.oscarrtorres.openbridgefx.models.ChatData;
import com.oscarrtorres.openbridgefx.models.ChatEntry;
import com.oscarrtorres.openbridgefx.models.TokenCostInfo;
import com.oscarrtorres.openbridgefx.models.YamlData;
import com.oscarrtorres.openbridgefx.services.AIRequestService;
import com.oscarrtorres.openbridgefx.services.AITokenService;
import com.oscarrtorres.openbridgefx.services.ChatService;
import com.oscarrtorres.openbridgefx.services.SpeechToTextService;
import com.oscarrtorres.openbridgefx.utils.FileUtils;
import com.oscarrtorres.openbridgefx.utils.Toast;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private VBox outputContainer;
    @FXML
    private VBox outputVbox;
    @FXML
    private ScrollPane outputScrollPane;
    @FXML
    private TabPane promptTabPane;
    @FXML
    private Tab promptTab;
    @FXML
    private TextArea promptTextArea;
    @FXML
    private Tab markdownTab;
    @FXML
    private WebView markdownWebView;
    @FXML
    private Tab curlTab;
    @FXML
    private TextArea curlTextArea;
    @FXML
    private Tab powerShellTab;
    @FXML
    private TextArea powerShellTextArea;
    @FXML
    private VBox parameterContainer;
    @FXML
    private ScrollPane parameterScrollPane;
    @FXML
    private VBox historyContainer;
    @FXML
    private MenuItem apiValuesMenuItem;
    @FXML
    private MenuItem voskModelsMenuItem;

    private static final double PARAMETER_HEIGHT = 150.0;
    private static final double MAX_SCROLLPANE_HEIGHT = 400.0;

    private final ChatService chatService = new ChatService();
    private final SpeechToTextService speechToTextService = new SpeechToTextService();
    private final AITokenService aiTokenService = new AITokenService();

    private YamlData yamlData = new YamlData();

    List<ChatData> chatHistory = new ArrayList<>();

    @FXML
    public void initialize() {
        promptTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateParameters(newValue);
        });

        // Set up a listener for tab selection changes
        promptTabPane.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldTab, newTab) -> updateTabContents(newTab));

        outputContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            outputScrollPane.setVvalue(1.0);  // Scrolls to the bottom
        });

        setKeyboardShortcuts();

        Platform.runLater(this::afterInitialize);
    }

    private void updateTabContents() {
        updateTabContents(markdownTab);
        updateTabContents(curlTab);
        updateTabContents(powerShellTab);
    }

    private void updateTabContents(Tab newTab) {
        if (newTab != null) {
            String parsedText = rawToParsedPrompt(promptTextArea.getText());
            if (newTab.getText().equals(markdownTab.getText())) {
                // set prompt markdown HTML
                WebEngine webEngine = markdownWebView.getEngine();
                webEngine.loadContent(getMarkdownHtml(parsedText)); // Load the converted HTML
            } else if (newTab.getText().equals(curlTab.getText())) {
                // set CURL text
                AIRequestService gptAIRequestService = new AIRequestService(yamlData, parsedText);
                curlTextArea.setText(gptAIRequestService.getCurlCommand());
            } else if (newTab.getText().equals(powerShellTab.getText())) {
                // set PowerShell text
                AIRequestService gptAIRequestService = new AIRequestService(yamlData, parsedText);
                powerShellTextArea.setText(gptAIRequestService.getPowerShellCurlCommand());
            }
        }
    }

    private String getMarkdownHtml(String text) {
        Parser parser = Parser.builder().build();
        org.commonmark.node.Node document = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private void afterInitialize() {
        Toast.setStage((Stage) outputScrollPane.getScene().getWindow());

        validateYamlFile();

        parameterScrollPane.setVisible(false);
        parameterScrollPane.setManaged(false);

        setChatHistory();
        speechToTextService.setController(this);
    }

    private void setKeyboardShortcuts() {
        // Set the accelerator for the 'API Values' menu item (Ctrl + E)
        apiValuesMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));

        // Set the accelerator for the 'Vosk Models' menu item (Ctrl + M)
        voskModelsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
    }

    public void updateModelType(String modelName) {
        aiTokenService.setModelType(ModelType.fromName(modelName).orElseThrow());
    }

    public SpeechToTextService getSpeechToTextService() {
        return speechToTextService;
    }

    private void setChatHistory() {
        chatHistory = chatService.getChatDataFromFiles();
        updateChatHistoryList();
    }

    private void updateChatHistoryList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        chatHistory.sort(Comparator.comparing(chatData -> {
            String timestamp = chatData.getTimestamp();
            if (timestamp == null) {
                // Place null timestamps first
                return null;
            }
            // Parse the timestamp string into LocalDateTime
            return LocalDateTime.parse(timestamp, formatter);
        }, Comparator.nullsFirst(Comparator.reverseOrder())));

        historyContainer.getChildren().clear();

        // Populate the VBox with chat history data
        for (ChatData chat : chatHistory) {
            createChatDataRow(chat);
        }
        Button newChatButton = new Button("New Chat");
        newChatButton.setCursor(javafx.scene.Cursor.HAND);

        // Add an event handler for the button
        newChatButton.setOnAction(event -> onNewChatClicked());

        // Center the button in a container if needed
        HBox buttonContainer = new HBox(newChatButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10)); // Add padding at the top

        // Add the button container to the history container
        historyContainer.getChildren().add(buttonContainer);
    }

    private void onNewChatClicked() {
        ChatData data = new ChatData(ChatService.getNewFileName());
        onChatHistoryClick(data);

        chatHistory.add(data);
    }

    private void createChatDataRow(ChatData chat) {
        ChatEntry lastEntry = chat.getLastChatEntry();

        // Create a VBox for the chat entry content
        VBox chatEntryContent = new VBox();
        chatEntryContent.setPadding(new Insets(10));
        chatEntryContent.setSpacing(5);
        chatEntryContent.setUserData(chat);

        String fullText = lastEntry.getFinalPrompt().replaceAll("\\s+", " ").trim();
        String displayedText = (fullText.length() > 30) ? fullText.substring(0, 30) + "..." : fullText;

        Text messageText = new Text(displayedText);
        messageText.setFont(new Font("Arial", 14));

        String subInfoText = String.format("%s | $%.8f", lastEntry.getTimestamp(), chat.getTotalCharge());
        Text subInfo = new Text(subInfoText);
        subInfo.setFont(new Font("Arial", 12));
        subInfo.setFill(Color.GRAY);

        chatEntryContent.getChildren().addAll(messageText, subInfo);

        // Create the Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setVisible(false); // Hidden by default
        deleteButton.setOnAction(event -> deleteChat(chat));

        // Overlay the button on the chat entry content
        StackPane chatEntryContainer = new StackPane();
        chatEntryContainer.getChildren().addAll(chatEntryContent, deleteButton);
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
        chatEntryContainer.setPadding(new Insets(0, 5, 0, 0));

        // Show and hide the button on hover
        chatEntryContainer.setOnMouseEntered(event -> deleteButton.setVisible(true));
        chatEntryContainer.setOnMouseExited(event -> deleteButton.setVisible(false));

        // Add the chat entry container to the history container
        historyContainer.getChildren().addAll(chatEntryContainer, new Separator());

        deleteButton.setCursor(javafx.scene.Cursor.HAND);
        chatEntryContent.setCursor(javafx.scene.Cursor.HAND);
        chatEntryContent.setOnMouseClicked(event -> onChatHistoryClick(chat));
    }

    private void deleteChat(ChatData chat) {
        // Create a confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this chat?");
        alert.setContentText("This action cannot be undone.");

        // Show the alert and wait for a user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed, proceed with deletion
            FileUtils.deleteChatFile(chat);
            setChatHistory();
            Toast.makeText("Deleted Chat!");
        } else {
            // User chose not to delete
            Toast.makeText("Deletion cancelled.");
        }
    }

    private void validateYamlFile() {
        yamlData = FileUtils.getYamlData();

        // ChatGPT models
        if (Objects.isNull(yamlData.getChatGpt().getModelList()) || yamlData.getChatGpt().getModelList().isEmpty()) {
            yamlData.getChatGpt().setModelList(new ArrayList<>(aiTokenService.getDefaultModelPricingMap().values()));
            FileUtils.saveYamlData(yamlData);
        }
        aiTokenService.setModelPricingList(yamlData.getChatGpt().getModelList());

        if (!yamlData.getChatGpt().isValid()) {
            showApiPropertiesDialog();
        }

        updateModelType(yamlData.getChatGpt().getModel());

        // speechToText values
        if (Objects.isNull(yamlData.getVosk().getModelList()) || yamlData.getVosk().getModelList().isEmpty()) {
            speechToTextService.fetchVoskModelList(yamlData);
        } else if (!Objects.isNull(yamlData.getVosk().getModel()) && !yamlData.getVosk().getModel().isEmpty()) {
            speechToTextService.loadModel(yamlData.getVosk().getModel());
        }
    }

    @FXML
    public void showApiPropertiesDialog() {
        ApiPropertiesDialog apiPropertiesDialog = new ApiPropertiesDialog(this, yamlData);
        apiPropertiesDialog.showDialog();
    }

    @FXML
    public void showVoskModelDialog() {
        speechToTextService.showVoskModelDialog(yamlData);
    }

    private void onChatHistoryClick(ChatData chatData) {
        chatService.setCurrentChatData(chatData);

        clearMessageBubbles();
        for (ChatEntry entry : chatData.getChatEntries()) {
            addMessageBubble(entry, true);
            addMessageBubble(entry, false);
        }
    }

    private void updateParameters(String prompt) {
        Map<String, String> currentParameters = getCurrentParameters();

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

    private @NotNull Map<String, String> getCurrentParameters() {
        Map<String, String> currentParameters = new HashMap<>();

        for (Node node : parameterContainer.getChildren()) {
            if (node instanceof VBox parameterSet) {
                HBox row = (HBox) parameterSet.getChildren().get(0);

                TextField keyField = (TextField) row.getChildren().get(1);
                TextArea valueField = (TextArea) parameterSet.getChildren().get(2);
                // Collect current parameters from the UI
                String key = keyField.getText().trim();
                String value = valueField.getText().trim();

                if (!key.isEmpty()) {
                    currentParameters.put(key, value);
                }
            }
        }
        return currentParameters;
    }

    @FXML
    public void addParameterField(String key) {
        addParameterField(key, "");
    }

    public void addParameterField(String key, String value) {
        // Outer VBox to stack the rows
        VBox parameterSet = new VBox(10);

        // First row (key + button)
        HBox keyRow = new HBox(10);
        Label keyLabel = new Label("Key:");
        TextField keyField = new TextField();
        keyField.setText(key);
        keyField.setPromptText("Enter your param key here...");

        keyField.setEditable(false);
        // Apply gray tint and border to the keyField
        keyField.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #d3d3d3; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-opacity: 1;");
        HBox.setHgrow(keyField, Priority.ALWAYS);

        // Second row (value text area)
        Label valueLabel = new Label("Value:");
        TextArea valueTextArea = new TextArea();
        valueTextArea.setText(value);
        valueTextArea.setPromptText("Enter your param value here...");
        valueTextArea.setPrefRowCount(3); // Adjust row count as needed

        valueTextArea.setPrefWidth(Double.MAX_VALUE);

        Button recordingButton = getRecordingButton(valueTextArea);

        keyRow.getChildren().addAll(keyLabel, keyField, recordingButton);

        parameterSet.getChildren().addAll(keyRow, valueLabel, valueTextArea);

        parameterContainer.getChildren().add(parameterSet);
    }

    private @NotNull Button getRecordingButton(TextArea valueField) {
        Button button = new Button("Start Recording");
        button.setOnAction(event -> {
            if (speechToTextService.isRecording()) {
                speechToTextService.stopRecording();
                if (!speechToTextService.isRecording()) {
                    button.setText("Start Recording");
                }
            } else {
                speechToTextService.startRecording(valueField);
                if (speechToTextService.isRecording()) {
                    button.setText("Stop Recording");
                }
            }
        });
        return button;
    }


    @FXML
    public void onSendButtonClick() {
        ChatEntry chatEntry = new ChatEntry();
        chatEntry.setModelName(aiTokenService.getModelType().getName());
        chatEntry.setTimestamp(ChatService.getCurrentTimestamp());

        chatEntry.setRawPrompt(promptTextArea.getText());
        Map<String, String> parameters = getCurrentParameters();
        chatEntry.setParameters(parameters);

        // Replace placeholders in prompt with parameter values
        String parsedPrompt = rawToParsedPrompt(chatEntry.getRawPrompt());

        chatEntry.setFinalPrompt(parsedPrompt);
        chatEntry.setPromptInfo(aiTokenService.getPromptInfo(parsedPrompt));

        addMessageBubble(chatEntry, true);

        // Create and start the GPT API service
        AIRequestService gptAIRequestService = getAiRequestService(chatEntry);
        gptAIRequestService.start(); // Start the service
    }

    private String rawToParsedPrompt(String prompt) {
        Map<String, String> parameters = getCurrentParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            prompt = prompt.replace(placeholder, entry.getValue());
        }
        return prompt;
    }

    private @NotNull AIRequestService getAiRequestService(ChatEntry chatEntry) {
        AIRequestService gptAIRequestService = new AIRequestService(yamlData, chatEntry.getFinalPrompt());

        gptAIRequestService.setOnSucceeded(event -> {
            String gptResponse = gptAIRequestService.getValue();
            chatEntry.setResponse(gptResponse);
            chatEntry.setResponseInfo(aiTokenService.getResponseInfo(gptResponse));

            addMessageBubble(chatEntry, false); // Add GPT response as received message

            chatService.getCurrentChatData().addChatEntry(chatEntry);
            chatService.saveChatData();

            updateChatHistoryList();
        });

        gptAIRequestService.setOnFailed(event -> {
            Throwable exception = gptAIRequestService.getException();
            chatEntry.setResponse("Error: " + exception.getMessage());
            addMessageBubble(chatEntry, false); // Handle error
        });
        return gptAIRequestService;
    }

    private void addMessageBubble(ChatEntry entry, boolean isSent) {
        String message = isSent ? entry.getFinalPrompt() : entry.getResponse();
        String timestamp = entry.getTimestamp();

        TextFlow messageTextFlow = getMessageTextFlow(message, isSent);

        String otherName = entry.getModelName().isBlank() ? "Other" : entry.getModelName();

        // Create sender label with timestamp at the top
        Label senderLabel = new Label(isSent ? "You (" + timestamp + ")" : otherName + " (" + timestamp + ")");
        senderLabel.setTextFill(Color.GRAY);
        senderLabel.setFont(new Font("Arial", 12));

        // Bottom label (e.g., token info or any other text)
        TokenCostInfo tokenCostInfo = isSent ? entry.getPromptInfo() : entry.getResponseInfo();
        Label bottomLabel = new Label(tokenCostInfo.toString());  // Replace with actual token info
        bottomLabel.setTextFill(Color.GRAY);
        bottomLabel.setFont(new Font("Arial", 12));

        VBox messageBubble = new VBox(5);
        messageBubble.getChildren().addAll(senderLabel, messageTextFlow, bottomLabel);
        messageBubble.setCursor(javafx.scene.Cursor.HAND);

        HBox messageMainParent = new HBox();
        messageMainParent.setSpacing(8);

        if (isSent) {
            messageMainParent.setAlignment(Pos.CENTER_RIGHT);

            messageBubble.setOnMouseClicked(event -> onSentMessageBubbleClick(entry));

            messageMainParent.getChildren().addAll(messageBubble);
        } else {
            // For received messages, align the dots to the right of the bubble
            messageMainParent.setAlignment(Pos.CENTER_LEFT);
            messageBubble.setOnMouseClicked(event -> onResponseMessageBubbleClick(entry));

            // Add the message bubble to the row
            messageMainParent.getChildren().add(messageBubble);
        }

        // Ensure dynamic height adjustments are allowed
        messageMainParent.setPrefHeight(Region.USE_COMPUTED_SIZE);
        messageBubble.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Add the message to the output container
        outputContainer.getChildren().add(messageMainParent);
    }

    private TextFlow getMessageTextFlow(String message, boolean isSent) {
        TextFlow messageTextFlow = new TextFlow(new Text(message));
        messageTextFlow.setMaxWidth(500);  // Set a maximum width for wrapping
        messageTextFlow.setPadding(new Insets(10));
        messageTextFlow.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-background-radius: 15;");
        if (isSent) {
            messageTextFlow.setStyle(messageTextFlow.getStyle() + "-fx-background-color: lightblue; -fx-text-fill: black;");
        } else {
            messageTextFlow.setStyle(messageTextFlow.getStyle() + "-fx-background-color: lightgreen; -fx-text-fill: black;");
        }

        return messageTextFlow;
    }

    private void clearMessageBubbles() {
        outputContainer.getChildren().clear();
    }

    private void onSentMessageBubbleClick(ChatEntry data) {
        promptTextArea.setText(data.getRawPrompt()); // will trigger updateParameters()
        updateTabContents();

        for (Node node : parameterContainer.getChildren()) {
            if (node instanceof VBox parameterSet) {
                HBox row = (HBox) parameterSet.getChildren().get(0);

                TextField keyField = (TextField) row.getChildren().get(1);
                TextArea valueField = (TextArea) parameterSet.getChildren().get(2);

                String key = keyField.getText().trim();

                valueField.setText(data.getParameters().getOrDefault(key, ""));
            }
        }

        parameterScrollPane.setVisible(!data.getParameters().isEmpty());
        parameterScrollPane.setManaged(!data.getParameters().isEmpty());

        copyTextToClipboard(data.getFinalPrompt());

        Toast.makeText("Copied to clipboard!");
    }

    private void onResponseMessageBubbleClick(ChatEntry data) {
        promptTextArea.setText(data.getResponse()); // will trigger updateParameters()
        updateTabContents();

        copyTextToClipboard(data.getResponse());

        Toast.makeText("Copied to clipboard!");
    }

    private void copyTextToClipboard(String text) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
}
