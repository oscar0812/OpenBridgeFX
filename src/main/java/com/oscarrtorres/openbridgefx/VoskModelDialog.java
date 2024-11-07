package com.oscarrtorres.openbridgefx;

import com.oscarrtorres.openbridgefx.models.Constants;
import com.oscarrtorres.openbridgefx.models.EnvData;
import com.oscarrtorres.openbridgefx.models.VoskModel;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VoskModelDialog {

    private final MainController controller;

    public VoskModelDialog(MainController controller) {
        this.controller = controller;
    }

    public void showDialog() {
        // Create a new Stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Available Vosk Models");

        // Create a VBox to hold the model information
        VBox modelList = new VBox(10);
        modelList.setPadding(new Insets(15));

        // Sample models with download links
        VoskModel[] models = new VoskModel[] {
                new VoskModel("https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"),
                new VoskModel("https://alphacephei.com/vosk/models/vosk-model-en-us-0.22.zip"),
                new VoskModel("https://alphacephei.com/vosk/models/vosk-model-en-us-0.42-gigaspeech.zip")
        };

        // Group for the radio buttons so that only one can be selected at a time
        ToggleGroup toggleGroup = new ToggleGroup();

        // Populate the VBox with models, download buttons, and radio buttons
        for (VoskModel model : models) {
            Label modelName = new Label(model.getName());
            String modelDir = Constants.MODELS_DIR_PATH + File.separator + model.getName().replace(".zip", "");
            boolean modelExists = Files.exists(Path.of(modelDir));

            // Set the button text based on whether the model exists or not
            Button downloadButton = new Button(modelExists ? "Re-download" : "Download");
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setVisible(false);  // Hide the progress bar initially

            // Create a radio button for each model, initially disabled
            RadioButton radioButton = new RadioButton("Set as default");
            radioButton.setDisable(!modelExists);
            radioButton.setToggleGroup(toggleGroup);  // Add radio button to the toggle group

            // Set consistent widths for the elements to ensure alignment
            modelName.setMinWidth(250);
            downloadButton.setMinWidth(220);
            radioButton.setMinWidth(220);

            // Layout the row with a consistent alignment
            HBox modelRow = new HBox(10, modelName, downloadButton, progressBar, radioButton);
            modelRow.setAlignment(Pos.CENTER_LEFT);  // Align the items to the left
            modelRow.setSpacing(15);  // Add consistent spacing between elements

            // Ensure that the progress bar doesn't take up too much space when invisible
            progressBar.setMaxWidth(Double.MAX_VALUE);

            // Update UI on progress change
            downloadButton.setOnAction(e -> {
                String modelNameText = model.getName();
                String modelUrl = model.getZipUrl();

                // Task to download the model with added exception handling
                Task<Void> downloadTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Path targetPath = Path.of(Constants.MODELS_DIR_PATH, modelUrl.substring(modelUrl.lastIndexOf('/') + 1));
                        Files.createDirectories(targetPath.getParent());

                        try (InputStream in = new URL(modelUrl).openStream();
                             OutputStream out = Files.newOutputStream(targetPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            long fileSize = new URL(modelUrl).openConnection().getContentLengthLong();
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
                            e.printStackTrace(); // Print exception to console for debugging
                            throw e; // Rethrow to trigger the onFailed event
                        }

                        // Extract the zip file once downloaded
                        extractZipFile(targetPath);
                        deleteZipFile(targetPath);
                        return null;
                    }
                };

                // Update UI on progress change
                progressBar.progressProperty().bind(downloadTask.progressProperty());
                progressBar.setVisible(true);

                // Handle task completion with additional debug logging
                downloadTask.setOnSucceeded(event -> {
                    System.out.println("Download completed successfully.");
                    this.controller.showInfoAlert("Download completed for: " + modelNameText);
                    progressBar.setVisible(false); // Hide progress bar when done

                    // Enable the radio button once the model is downloaded and extracted
                    radioButton.setDisable(false);
                });
                downloadTask.setOnFailed(event -> {
                    System.out.println("Download failed.");
                    Throwable exception = downloadTask.getException();
                    if (exception != null) {
                        System.err.println("Error: " + exception.getMessage());
                    }
                    this.controller.showErrorAlert("Download failed for: " + modelNameText);
                    progressBar.setVisible(false); // Hide progress bar when done
                });

                // Start the task in a new thread
                new Thread(downloadTask).start();
            });

            modelList.getChildren().add(modelRow);
        }

        // Create a Scene and set it in the Stage
        Scene scene = new Scene(modelList, 650, 200);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    // Method to extract a zip file
    private void extractZipFile(Path zipFilePath) {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path extractedPath = Paths.get(zipFilePath.getParent().toString(), entry.getName());

                // Create directories if necessary
                if (entry.isDirectory()) {
                    Files.createDirectories(extractedPath);
                } else {
                    // Extract the file
                    try (OutputStream out = Files.newOutputStream(extractedPath, StandardOpenOption.CREATE)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.controller.showErrorAlert("Error extracting the zip file: " + e.getMessage());
        }
    }

    private void deleteZipFile(Path zipFilePath) {
        try {
            Files.delete(zipFilePath);
            System.out.println("Deleted zip file: " + zipFilePath);
        } catch (IOException e) {
            System.err.println("Failed to delete zip file: " + zipFilePath);
            e.printStackTrace();
        }
    }
}
