package com.oscarrtorres.openbridgefx.dialogs;

import com.knuddels.jtokkit.api.ModelType;
import com.oscarrtorres.openbridgefx.MainController;
import com.oscarrtorres.openbridgefx.models.EnvData;
import com.oscarrtorres.openbridgefx.utils.FileUtils;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Objects;
import java.util.Optional;

public class ApiEnvFileDialog {

    private final MainController controller; // Reference to the MainController
    private final EnvData envData;

    public ApiEnvFileDialog(MainController controller, EnvData envData) {
        this.controller = controller;
        this.envData = envData;
    }

    public void showDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        String headerText = envData.hasValidApiData() ? "Update .env file" : "The .env file is missing!";
        alert.setTitle("Configuration Missing");
        alert.setHeaderText(headerText);
        alert.setContentText("Please enter your configuration values.");

        // Create fields for the required configuration
        TextField apiKeyField = new TextField();
        apiKeyField.setPromptText("Enter API Key");

        if(!Objects.isNull(envData.getApiKey())) {
            apiKeyField.setText(envData.getApiKey());
        }

        TextField apiUrlField = new TextField();
        apiUrlField.setPromptText("Enter API URL");

        if(!Objects.isNull(envData.getApiUrl())) {
            apiUrlField.setText(envData.getApiUrl());
        }

        // Create a ComboBox for model selection
        ComboBox<String> modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll(
                ModelType.GPT_4O.getName(),
                ModelType.GPT_4O_MINI.getName(),
                ModelType.GPT_4_32K.getName(),
                ModelType.GPT_4_TURBO.getName(),
                ModelType.GPT_3_5_TURBO.getName(),
                ModelType.GPT_3_5_TURBO_16K.getName()
        );
        modelComboBox.setPromptText("Select Model");

        // Create an error label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false); // Initially hidden

        // Add fields to the dialog
        VBox dialogPaneContent = new VBox(8);
        dialogPaneContent.getChildren().addAll(
                new Label("API Key:"), apiKeyField,
                new Label("API URL:"), apiUrlField,
                new Label("Model:"), modelComboBox,
                errorLabel // Add the error label to the dialog
        );

        if (!Objects.isNull(envData.getModel()) && modelComboBox.getItems().contains(envData.getModel())) {
            modelComboBox.setValue(envData.getModel());
        }

        alert.getDialogPane().setContent(dialogPaneContent);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save");
        alert.getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);

        while (true) { // Keep showing the dialog until valid input is received
            // Show the dialog and wait for the user response
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == saveButtonType) {
                String apiKey = apiKeyField.getText().trim();
                String apiUrl = apiUrlField.getText().trim();
                String selectedModel = modelComboBox.getValue();

                // Validation: Check for empty fields
                if (apiKey.isEmpty() || apiUrl.isEmpty() || selectedModel == null) {
                    errorLabel.setText("All fields must be filled!");
                    errorLabel.setVisible(true); // Show the error message
                } else {
                    // Hide error message if input is valid
                    errorLabel.setVisible(false);

                    envData.setApiKey(apiKey);
                    envData.setApiUrl(apiUrl);
                    envData.setModel(selectedModel);
                    FileUtils.saveEnvFile(envData);
                    break; // Break the loop if everything is valid
                }
            } else {
                // User clicked Cancel, exit the application
                if(!envData.hasValidApiData()) {
                    System.exit(0);
                }
                break;
            }
        }
    }
}
