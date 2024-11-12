package com.oscarrtorres.openbridgefx.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscarrtorres.openbridgefx.models.SpeechToTextData;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Map;

public class SpeechToTextRecordingThread implements Runnable {

    private final TextArea valueField;
    private final SpeechToTextData data;
    private final StringBuilder accumulatedText = new StringBuilder();
    private volatile boolean running = true;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON parsing

    public SpeechToTextRecordingThread(TextArea valueField, SpeechToTextData data) throws IOException {
        this.valueField = valueField;
        this.data = data;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Line not supported");
            return;
        }

        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();

            byte[] buffer = new byte[4096];
            while (running) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead < 0) break;

                if (this.data.getRecognizer().acceptWaveForm(buffer, bytesRead)) {
                    String result = this.data.getRecognizer().getResult();
                    processFinalResult(result);
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void processFinalResult(String resultJson) {
        try {
            // Parse the JSON result to get the "text" field only if it's present
            Map<String, String> result = objectMapper.readValue(resultJson, Map.class);
            if (result.containsKey("text")) {
                accumulatedText.append(result.get("text")).append(" ");
                Platform.runLater(() -> valueField.setText(accumulatedText.toString().trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}