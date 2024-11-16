package com.oscarrtorres.openbridgefx.dialogs;

import com.oscarrtorres.openbridgefx.MainController;
import com.oscarrtorres.openbridgefx.models.YamlData;
import com.oscarrtorres.openbridgefx.utils.FileUtils;
import com.oscarrtorres.openbridgefx.utils.Toast;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public class ApiPropertiesDialog {

    private final MainController controller; // Reference to the MainController
    private final YamlData yamlData;

    public void showDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("API Properties");
        alert.setHeaderText("Update connection properties");
        alert.setContentText("Please enter your values");

        // Create fields for the required configuration
        TextField apiKeyField = new TextField();
        apiKeyField.setPromptText("Enter API Key");

        if (!Objects.isNull(yamlData.getChatGpt().getApiKey())) {
            apiKeyField.setText(yamlData.getChatGpt().getApiKey());
        }

        TextField apiUrlField = new TextField();
        apiUrlField.setPromptText("Enter API URL");

        if (!Objects.isNull(yamlData.getChatGpt().getApiUrl())) {
            apiUrlField.setText(yamlData.getChatGpt().getApiUrl());
        }

        // Create a ComboBox for model selection
        ComboBox<String> modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll(
                yamlData.getChatGpt().getModelList()
                        .stream().map(m -> m.getModelType().getName()).toList()
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

        if (!Objects.isNull(yamlData.getChatGpt().getModel()) && modelComboBox.getItems().contains(yamlData.getChatGpt().getModel())) {
            modelComboBox.setValue(yamlData.getChatGpt().getModel());
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

                    boolean updatedModel = !selectedModel.equals(yamlData.getChatGpt().getModel());

                    yamlData.getChatGpt().setApiKey(apiKey);
                    yamlData.getChatGpt().setApiUrl(apiUrl);
                    yamlData.getChatGpt().setModel(selectedModel);
                    FileUtils.saveYamlData(yamlData);

                    if (updatedModel) {
                        this.controller.updateModelType(selectedModel);
                    }
                    Toast.makeText("Api properties set");
                    break; // Break the loop if everything is valid
                }
            } else {
                // User clicked Cancel, exit the application
                if (!yamlData.getChatGpt().isValid()) {
                    System.exit(0);
                }
                break;
            }
        }
    }
}
