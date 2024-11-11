package com.oscarrtorres.openbridgefx;

import com.knuddels.jtokkit.api.ModelType;
import com.oscarrtorres.openbridgefx.dialogs.ApiYamlDataDialog;
import com.oscarrtorres.openbridgefx.models.*;
import com.oscarrtorres.openbridgefx.services.ApiService;
import com.oscarrtorres.openbridgefx.services.ChatService;
import com.oscarrtorres.openbridgefx.services.SpeechToTextService;
import com.oscarrtorres.openbridgefx.services.TokenService;
import com.oscarrtorres.openbridgefx.utils.FileUtils;
import com.oscarrtorres.openbridgefx.utils.Toast;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import javafx.util.Duration;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private TextArea promptTextArea;
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

    private static final double PARAMETER_HEIGHT = 200.0;
    private static final double MAX_SCROLLPANE_HEIGHT = 900.0;

    private final ChatService chatService = new ChatService();
    private final SpeechToTextService speechToTextService = new SpeechToTextService();

    private TokenService tokenService;
    private YamlData yamlData = new YamlData();

    List<ChatData> chatHistory = new ArrayList<>();

    @FXML
    public void initialize() {
        setKeyboardShortcuts();
        validateYamlFile();

        promptTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateParameters(newValue);
        });

        outputContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            outputScrollPane.setVvalue(1.0);  // Scrolls to the bottom
        });

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

    public SpeechToTextService getSpeechToTextService() {
        return speechToTextService;
    }

    public ScrollPane getOutputScrollPane() {
        return outputScrollPane;
    }

    public void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    }

    private void createChatDataRow(ChatData chat) {
        ChatEntry lastEntry = chat.getLastChatEntry();
        // Create a VBox for the chat entry
        VBox chatEntry = new VBox();
        chatEntry.setPadding(new Insets(10)); // Set padding for the entire chat entry
        chatEntry.setSpacing(5); // Set spacing between elements
        chatEntry.setUserData(chat);

        String fullText = lastEntry.getFinalPrompt();
        String displayedText = (fullText.length() > 30) ? fullText.substring(0, 30) + "..." : fullText;

        Text messageText = new Text(displayedText);
        messageText.setFont(new Font("Arial", 14));

        // Create the charge text
        String subInfoText = lastEntry.getTimestamp() + " | " + chat.getTotalCharge();
        Text subInfo = new Text(subInfoText);
        subInfo.setFont(new Font("Arial", 12));
        subInfo.setFill(Color.GRAY);

        // Create a horizontal line (separator)
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: lightgray;"); // Customize line color

        // Add all elements to the chat entry
        chatEntry.getChildren().addAll(messageText, subInfo);

        // Add the chat entry to the history VBox
        historyContainer.getChildren().addAll(chatEntry, separator);

        chatEntry.setCursor(javafx.scene.Cursor.HAND);
        chatEntry.setOnMouseClicked(event -> onChatHistoryClick(chat));
    }

    private void validateYamlFile() {
        File yamlFile = new File(Constants.PROJECT_YAML_FILE_PATH);
        if (!yamlFile.exists()) {
            showApiYamlDialog();
        }

        // file exists, but does it have all the required values?
        yamlData = FileUtils.getYamlData();

        if (!yamlData.getChatGpt().isValid()) {
            showApiYamlDialog();
        }

        tokenService = new TokenService(ModelType.fromName(yamlData.getChatGpt().getModel()).orElseThrow());

        if (Objects.isNull(yamlData.getVosk().getModelList()) || yamlData.getVosk().getModelList().isEmpty()) {
            speechToTextService.fetchVoskModelList(yamlData);
        } else if (!Objects.isNull(yamlData.getVosk().getModel()) && !yamlData.getVosk().getModel().isEmpty()) {
            speechToTextService.loadModel(yamlData.getVosk().getModel());
        }
    }

    @FXML
    public void showApiYamlDialog() {
        ApiYamlDataDialog apiYamlDataDialog = new ApiYamlDataDialog(this, yamlData);
        apiYamlDataDialog.showDialog(); // Show the dialog
    }

    @FXML
    public void showVoskModelDialog() {
        speechToTextService.showVoskModelDialog(yamlData);
    }

    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
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

        // Button to start/stop recording
        Button button1 = new Button("Start Recording");
        button1.setOnAction(event -> {
            if (speechToTextService.isRecording()) {
                speechToTextService.stopRecording();
                if (!speechToTextService.isRecording()) {
                    button1.setText("Start Recording");
                }
            } else {
                speechToTextService.startRecording(valueField);
                if (speechToTextService.isRecording()) {
                    button1.setText("Stop Recording");
                }
            }
        });

        HBox.setHgrow(valueField, Priority.ALWAYS);
        parameterSet.getChildren().addAll(keyLabel, keyField, valueLabel, valueField, button1);
        parameterContainer.getChildren().add(parameterSet);
    }


    @FXML
    public void onSendButtonClick() {
        ChatEntry chatEntry = new ChatEntry();
        chatEntry.setTimestamp(chatService.getCurrentTimestamp());

        chatEntry.setRawPrompt(promptTextArea.getText());
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
        chatEntry.setParameters(parameters);

        // Replace placeholders in prompt with parameter values
        String parsedPrompt = chatEntry.getRawPrompt();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            parsedPrompt = parsedPrompt.replace(placeholder, entry.getValue());
        }

        chatEntry.setFinalPrompt(parsedPrompt);
        chatEntry.setPromptInfo(tokenService.getPromptInfo(parsedPrompt));

        addMessageBubble(chatEntry, true);

        // Create and start the GPT API service
        ApiService gptApiService = new ApiService(chatEntry.getFinalPrompt(), yamlData);

        gptApiService.setOnSucceeded(event -> {
            String gptResponse = gptApiService.getValue();
            chatEntry.setResponse(gptResponse);
            chatEntry.setResponseInfo(tokenService.getResponseInfo(gptResponse));

            addMessageBubble(chatEntry, false); // Add GPT response as received message

            chatService.getCurrentChatData().addChatEntry(chatEntry);
            chatService.saveChatData();

            updateChatHistoryList();
        });

        gptApiService.setOnFailed(event -> {
            Throwable exception = gptApiService.getException();
            chatEntry.setResponse("Error: " + exception.getMessage());
            addMessageBubble(chatEntry, false); // Handle error
        });

        gptApiService.start(); // Start the service
    }

    private void addMessageBubble(ChatEntry entry, boolean isSent) {
        String message = isSent ? entry.getFinalPrompt() : entry.getResponse();
        String timestamp = entry.getTimestamp();

        // Using TextFlow for better text wrapping and dynamic height adjustment
        TextFlow messageTextFlow = new TextFlow(new Text(message));
        messageTextFlow.setMaxWidth(500);  // Set a maximum width for wrapping
        messageTextFlow.setPadding(new Insets(10));
        messageTextFlow.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-background-radius: 15;");

        // Create sender label with timestamp at the top
        Label senderLabel = new Label(isSent ? "You (" + timestamp + ")" : "Other (" + timestamp + ")");
        senderLabel.setTextFill(Color.GRAY);
        senderLabel.setFont(new Font("Arial", 12));

        // Bottom label (e.g., token info or any other text)
        TokenCostInfo tokenCostInfo = isSent ? entry.getPromptInfo() : entry.getResponseInfo();
        Label bottomLabel = new Label(tokenCostInfo.toString());  // Replace with actual token info
        bottomLabel.setTextFill(Color.GRAY);
        bottomLabel.setFont(new Font("Arial", 12));

        VBox messageBubble = new VBox(5);
        messageBubble.setCursor(javafx.scene.Cursor.HAND);
        messageBubble.setUserData(entry);
        messageBubble.getChildren().addAll(senderLabel, messageTextFlow, bottomLabel);

        HBox messageMainParent = new HBox();
        messageMainParent.getChildren().add(messageBubble);

        if (isSent) {
            messageMainParent.setAlignment(Pos.CENTER_RIGHT);
            messageTextFlow.setStyle(messageTextFlow.getStyle() + "-fx-background-color: lightblue; -fx-text-fill: black;");
            messageBubble.setOnMouseClicked(event -> onSentMessageBubbleClick(messageBubble));
        } else {
            messageMainParent.setAlignment(Pos.CENTER_LEFT);
            messageTextFlow.setStyle(messageTextFlow.getStyle() + "-fx-background-color: lightgreen; -fx-text-fill: black;");
            messageBubble.setOnMouseClicked(event -> onResponseMessageBubbleClick(messageBubble));
        }

        // Ensure dynamic height adjustments are allowed
        messageMainParent.setPrefHeight(Region.USE_COMPUTED_SIZE);
        messageBubble.setPrefHeight(Region.USE_COMPUTED_SIZE);

        setMessageBubbleHoverMenu(messageBubble, entry, message, isSent);

        outputContainer.getChildren().add(messageMainParent);
    }

    // show menu when you hover over bubbles
    private void setMessageBubbleHoverMenu(VBox messageBubble, ChatEntry entry, String message, boolean isSent) {
        // Create ContextMenu with options
        ContextMenu contextMenu = new ContextMenu();

        // Option 1 to replace TextFlow content with WebView
        MenuItem option1 = new MenuItem("Show in WebView");
        option1.setOnAction(event -> showMarkdown(messageBubble, message, isSent));

        // Option 2 (example of another option)
        MenuItem option2 = new MenuItem("Option 2");
        option2.setOnAction(event -> handleOption2(entry));

        // Add options to the context menu
        contextMenu.getItems().addAll(option1, option2);

        PauseTransition hoverTimer = new PauseTransition(Duration.seconds(2));

        messageBubble.setOnMouseEntered((MouseEvent event) -> {
            hoverTimer.playFromStart();
        });

        // Start the hover timer and show the menu at the mouse coordinates after x seconds
        hoverTimer.setOnFinished(event -> {
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    contextMenu.show(messageBubble, p.x, p.y);
                }
        );

        // Cancel the timer if mouse exits before the delay
        messageBubble.setOnMouseExited(event -> hoverTimer.stop());
    }


    // Method to handle the first option click and display Markdown as HTML in WebView
    private void showMarkdown(VBox bubbleContainer, String markdownMessage, boolean isSent) {
        // Convert Markdown to HTML using CommonMark
        Parser parser = Parser.builder().build();
        org.commonmark.node.Node document = parser.parse(markdownMessage);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        // Remove existing TextFlow from the bubble container
        bubbleContainer.getChildren().removeIf(node -> node instanceof TextFlow);

        // Create a StackPane to hold the WebView with the bubble's styling
        StackPane webViewContainer = new StackPane();
        webViewContainer.setMaxWidth(500); // Match the original width
        webViewContainer.setPadding(new Insets(10));

        // Apply bubble styling
        String bubbleStyle = isSent
                ? "-fx-background-color: lightblue; -fx-background-radius: 15; -fx-padding: 10;"
                : "-fx-background-color: lightgreen; -fx-background-radius: 15; -fx-padding: 10;";
        webViewContainer.setStyle(bubbleStyle);

        // Create a WebView and load the HTML content
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(htmlContent); // Load the converted HTML

        // Add the WebView to the styled container
        webViewContainer.getChildren().add(webView);

        // Add the styled WebView container to the bubble container
        bubbleContainer.getChildren().add(1, webViewContainer);  // Add in place of the TextFlow
    }

    // Sample method for the second option
    private void handleOption2(ChatEntry entry) {
        System.out.println("Option 2 clicked for entry: " + entry);
    }

    private void clearMessageBubbles() {
        outputContainer.getChildren().clear();
    }

    private void onSentMessageBubbleClick(VBox messageBubble) {
        ChatEntry data = (ChatEntry) messageBubble.getUserData();

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

        copyTextToClipboard(data.getFinalPrompt());

        Window stage = outputScrollPane.getScene().getWindow();
        Toast.makeText(stage, "Copied to clipboard!");
    }

    private void onResponseMessageBubbleClick(VBox messageBubble) {
        ChatEntry data = (ChatEntry) messageBubble.getUserData();

        promptTextArea.setText(data.getResponse()); // will trigger updateParameters()

        copyTextToClipboard(data.getResponse());

        Window stage = outputScrollPane.getScene().getWindow();
        Toast.makeText(stage, "Copied to clipboard!");
    }

    private void copyTextToClipboard(String text) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
}
