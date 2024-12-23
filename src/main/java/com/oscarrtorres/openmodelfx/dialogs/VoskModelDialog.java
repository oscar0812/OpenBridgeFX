package com.oscarrtorres.openmodelfx.dialogs;

import com.oscarrtorres.openmodelfx.MainController;
import com.oscarrtorres.openmodelfx.models.Constants;
import com.oscarrtorres.openmodelfx.models.VoskModel;
import com.oscarrtorres.openmodelfx.models.YamlData;
import com.oscarrtorres.openmodelfx.utils.FileUtils;
import com.oscarrtorres.openmodelfx.utils.Toast;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class VoskModelDialog {

    private final MainController controller;
    private final YamlData yamlData;

    public void showDialog() {
        // Create an alert dialog with a custom content
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Download Vosk Models");
        alert.setHeaderText(null);

        // Create a VBox to hold the model information
        VBox modelList = new VBox(10);
        modelList.setPadding(new Insets(15));

        // Sample models with download links
        List<VoskModel> models = yamlData.getVosk().getModelList();

        // Group for the radio buttons so that only one can be selected at a time
        ToggleGroup toggleGroup = new ToggleGroup();
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);

        // Initially disable the OK button
        okButton.setDisable(true);

        // Add a listener to enable the OK button only when a model is selected
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            okButton.setDisable(newToggle == null); // Disable if no selection
        });

        // Populate the VBox with models, download buttons, and radio buttons
        for (VoskModel model : models) {
            Label modelName = new Label(model.getLanguage() + " - " + model.getName());
            String modelDir = Constants.MODELS_DIR_PATH + File.separator + model.getName().replace(".zip", "");
            boolean modelExists = Files.exists(Path.of(modelDir));

            // Set the button text based on whether the model exists or not
            Button downloadButton = new Button(modelExists ? "Download Again" : "Download");
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setVisible(false);
            progressBar.setMinWidth(150);
            progressBar.setPrefWidth(200);
            progressBar.setMaxWidth(Double.MAX_VALUE);

            // Create a radio button for each model, initially disabled
            RadioButton radioButton = new RadioButton("Set as default");
            radioButton.setDisable(!modelExists);
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(model);

            if (modelExists && !Objects.isNull(yamlData.getVosk().getModel()) && yamlData.getVosk().getModel().equals(model.getName())) {
                radioButton.setSelected(true);
            }

            // Set consistent widths for the elements to ensure alignment
            modelName.setMinWidth(250);
            downloadButton.setMinWidth(220);
            radioButton.setMinWidth(220);

            // Layout the row with a consistent alignment
            HBox modelRow = new HBox(10, modelName, downloadButton, progressBar, radioButton);
            modelRow.setSpacing(15);
            modelRow.setFillHeight(true);

            downloadButton.setOnAction(e -> downloadClicked(model, downloadButton, okButton, progressBar, radioButton));

            modelList.getChildren().add(modelRow);
        }

        // Wrap the modelList VBox in a ScrollPane for vertical scrolling only
        ScrollPane scrollPane = new ScrollPane(modelList);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scrolling
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Enable vertical scrolling as needed
        scrollPane.setPrefHeight(400); // Set a preferred height for scrolling

        // Set the content of the alert to the ScrollPane
        alert.getDialogPane().setContent(scrollPane);
        alert.getDialogPane().setPrefSize(950, 450); // Adjust as needed

        okButton.setOnAction(event -> okButtonClicked(toggleGroup));

        alert.showAndWait();
    }

    public void downloadClicked(VoskModel model, Button downloadButton, Button okButton, ProgressBar progressBar, RadioButton radioButton) {
        String modelNameText = model.getName();
        String modelUrl = model.getZipUrl();
        downloadButton.setDisable(true);
        okButton.setDisable(true);

        // Task to download the model with added exception handling
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Path zipFilePath = Path.of(Constants.MODELS_DIR_PATH, modelUrl.substring(modelUrl.lastIndexOf('/') + 1));
                Files.createDirectories(zipFilePath.getParent());

                try (InputStream in = new URI(modelUrl).toURL().openStream();
                     OutputStream out = Files.newOutputStream(zipFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    long fileSize = new URI(modelUrl).toURL().openConnection().getContentLengthLong();
                    if (fileSize == -1) {
                        throw new Exception("Could not retrieve file size, download may be incomplete.");
                    }

                    byte[] buffer = new byte[4096];
                    long totalRead = 0;
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                        updateProgress(totalRead, fileSize);
                    }

                    if (totalRead != fileSize) {
                        throw new Exception("File download was incomplete. Expected: " + fileSize + " bytes, but got: " + totalRead + " bytes.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
                FileUtils.extractZipFile(zipFilePath, true);
                FileUtils.deleteFilePath(zipFilePath);
                return null;
            }
        };

        progressBar.progressProperty().bind(downloadTask.progressProperty());
        progressBar.setVisible(true);

        downloadTask.setOnSucceeded(event -> {
            Toast.makeText("Download completed for: " + modelNameText);
            downloadButton.setText("Download Again");
            progressBar.setVisible(false);
            radioButton.setDisable(false);
            radioButton.setSelected(true);
            downloadButton.setDisable(false);
            okButton.setDisable(false);
        });
        downloadTask.setOnFailed(event -> {
            Throwable exception = downloadTask.getException();
            if (exception != null) {
                exception.printStackTrace();
            }
            Toast.makeText("Download failed for: " + modelNameText);
            progressBar.setVisible(false);
            downloadButton.setDisable(false);
            okButton.setDisable(false);
        });

        new Thread(downloadTask).start();
    }

    public void okButtonClicked(ToggleGroup toggleGroup) {
        RadioButton selectedButton = (RadioButton) toggleGroup.getSelectedToggle();
        if (selectedButton != null) {
            VoskModel voskModel = (VoskModel) selectedButton.getUserData();
            System.out.println("Selected model: " + voskModel.getName());

            yamlData.getVosk().setModel(voskModel.getName());

            FileUtils.saveYamlData(yamlData);

            this.controller.getSpeechToTextService().loadModel(voskModel.getName());
        } else {
            Alert warningAlert = new Alert(Alert.AlertType.WARNING, "Please select a model before proceeding.");
            warningAlert.showAndWait();
        }
    }
}
