package com.oscarrtorres.openbridgefx.services;

import com.oscarrtorres.openbridgefx.MainController;
import com.oscarrtorres.openbridgefx.dialogs.VoskModelDialog;
import com.oscarrtorres.openbridgefx.models.Constants;
import com.oscarrtorres.openbridgefx.models.SpeechToTextData;
import com.oscarrtorres.openbridgefx.models.YamlData;
import com.oscarrtorres.openbridgefx.utils.FileUtils;
import com.oscarrtorres.openbridgefx.utils.Toast;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechToTextService {
    private MainController controller;
    private SpeechToTextData speechToTextData;
    private SpeechToTextThread speechToTextThread;
    private Thread speechThread;
    private boolean isRecording;

    public SpeechToTextService() {

    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    public SpeechToTextData getSpeechRecognizerData() {
        return speechToTextData;
    }

    public void setSpeechRecognizerData(SpeechToTextData speechToTextData) {
        this.speechToTextData = speechToTextData;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void showVoskModelDialog(YamlData yamlData) {
        VoskModelDialog voskModelDialog = new VoskModelDialog(this.controller, yamlData);
        voskModelDialog.showDialog();
    }

    public void startRecording(TextField valueField) {
        if (Objects.isNull(speechToTextData)) {
            this.controller.showVoskModelDialog();
            return;
        } else if (!speechToTextData.isLoaded()) {
            Window stage = this.controller.getOutputScrollPane().getScene().getWindow();
            Toast.makeText(stage, "The speech model is still loading...");
            return;
        }

        try {
            speechToTextThread = new SpeechToTextThread(valueField, speechToTextData);
            isRecording = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        speechThread = new Thread(speechToTextThread);
        speechThread.start();
    }

    public void stopRecording() {
        if (speechToTextThread != null) {
            speechToTextThread.stop();
            speechThread.interrupt();
        }
        isRecording = false;
    }

    public void fetchVoskModelList(YamlData yamlData) {
        MainController mainController = this.controller;
        Task<List<String>> loadDataTask = new Task<>() {
            @Override
            protected List<String> call() {
                String urlString = "https://alphacephei.com/vosk/models"; // Replace with your target URL
                ArrayList<String> zipLinks = new ArrayList<>();

                try {
                    // Open a connection to the URL and create a BufferedReader to read the content
                    URL url = new URL(urlString);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                    String line;
                    // Regex pattern to match hrefs that end with .zip
                    Pattern pattern = Pattern.compile("href=[\"']([^\"'>]+\\.zip)[\"']");

                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        while (matcher.find()) {
                            // Print each href that ends with .zip
                            zipLinks.add(matcher.group(1));
                        }
                    }

                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return zipLinks;
            }

            @Override
            protected void succeeded() {
                List<String> zipLinks = getValue();
                yamlData.getVosk().setModelList(zipLinks);
                FileUtils.saveYamlData(yamlData);
                System.out.println("Speech model list fetched successfully.");
            }

            @Override
            protected void failed() {
                mainController.showVoskModelDialog();
            }
        };

        // Run the task in the background
        new Thread(loadDataTask).start();
    }

    public void loadModel(@NotNull String voskModelName) {
        SpeechToTextService service = this;

        Task<Void> loadDataTask = new Task<>() {
            SpeechToTextData speechRecognizerData = service.getSpeechRecognizerData();
            @Override
            protected Void call() {
                // Perform the model loading in the background thread
                if (!Objects.isNull(speechRecognizerData) && voskModelName.equals(speechRecognizerData.getModelName())) {
                    System.out.println("Speech model was already loaded.");
                    return null;
                }
                speechRecognizerData = new SpeechToTextData(Constants.MODELS_DIR_PATH + File.separator + voskModelName);
                service.setSpeechRecognizerData(speechRecognizerData);
                speechRecognizerData.loadModel();
                return null;
            }

            @Override
            protected void succeeded() {
                System.out.println("Speech model loaded successfully.");
            }

            @Override
            protected void failed() {
                controller.showVoskModelDialog();
            }
        };

        // Run the task in the background
        new Thread(loadDataTask).start();
    }
}
