package com.oscarrtorres.openbridgefx.models;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.IOException;
import java.util.Objects;

public class SpeechRecognizerData {
    private String modelPath;
    private String modelName;
    private Model model;
    private Recognizer recognizer;

    public SpeechRecognizerData(String modelPath) {
        this.modelPath = modelPath;
        this.modelName = modelPath.substring(modelPath.lastIndexOf('/') + 1);
    }

    public void loadModel() {
        try {
            this.model = new Model(modelPath);
            this.recognizer = new Recognizer(model, 16000);
            this.recognizer.setPartialWords(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Recognizer getRecognizer() {
        return recognizer;
    }

    public void setRecognizer(Recognizer recognizer) {
        this.recognizer = recognizer;
    }

    public boolean isLoaded() {
        return !Objects.isNull(this.getModel());
    }
}
