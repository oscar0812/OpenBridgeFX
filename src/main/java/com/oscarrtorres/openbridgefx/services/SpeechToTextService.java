package com.oscarrtorres.openbridgefx.services;

import com.oscarrtorres.openbridgefx.MainController;
import com.oscarrtorres.openbridgefx.dialogs.VoskModelDialog;
import com.oscarrtorres.openbridgefx.models.Constants;
import com.oscarrtorres.openbridgefx.models.SpeechToTextData;
import com.oscarrtorres.openbridgefx.models.VoskModel;
import com.oscarrtorres.openbridgefx.models.YamlData;
import com.oscarrtorres.openbridgefx.utils.FileUtils;
import com.oscarrtorres.openbridgefx.utils.Toast;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpeechToTextService {
    private MainController controller;
    private SpeechToTextData speechToTextData;
    private SpeechToTextRecordingThread speechToTextRecordingThread;
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

    public void startRecording(TextArea valueField) {
        if (Objects.isNull(speechToTextData)) {
            this.controller.showVoskModelDialog();
            return;
        } else if (!speechToTextData.isLoaded()) {
            Toast.makeText("The speech model is still loading...");
            return;
        }

        try {
            speechToTextRecordingThread = new SpeechToTextRecordingThread(valueField, speechToTextData);
            isRecording = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        speechThread = new Thread(speechToTextRecordingThread);
        speechThread.start();
    }

    public void stopRecording() {
        if (speechToTextRecordingThread != null) {
            speechToTextRecordingThread.stop();
            speechThread.interrupt();
        }
        isRecording = false;
    }

    public void fetchVoskModelList(YamlData yamlData) {
        MainController mainController = this.controller;
        Task<List<VoskModel>> loadDataTask = new Task<>() {
            @Override
            protected List<VoskModel> call() {
                List<VoskModel> models = new ArrayList<>();
                try {
                    // Connect to the URL and parse the HTML
                    Document doc = Jsoup.connect("https://alphacephei.com/vosk/models").get();

                    Element firstTableBody = doc.select("table tbody").first();

                    if (firstTableBody != null) {
                        Elements rows = firstTableBody.select("tr");
                        String currentType = "";

                        for (Element row : rows) {
                            Elements cells = row.select("td");

                            if (!cells.isEmpty() && !Objects.requireNonNull(cells.first()).select("strong").isEmpty()) {
                                currentType = Objects.requireNonNull(cells.first()).text().trim();
                            } else {
                                // Check for .zip URLs in the cells
                                Elements links = row.select("a[href^=https://alphacephei.com/vosk/models][href$=.zip]");
                                for (Element link : links) {
                                    String href = link.attr("href");
                                    models.add(new VoskModel(href, currentType));
                                }
                            }
                        }
                    } else {
                        System.out.println("No table body found.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return models;
            }

            @Override
            protected void succeeded() {
                yamlData.getVosk().setModelList(getValue());
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
