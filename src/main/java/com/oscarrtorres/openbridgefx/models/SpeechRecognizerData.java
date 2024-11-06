package com.oscarrtorres.openbridgefx.models;

import com.oscarrtorres.openbridgefx.services.SpeechRecognizerThread;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.IOException;

public class SpeechRecognizerData {
    private String modelPath;
    private Model model;
    private Recognizer recognizer;

    public SpeechRecognizerData(String modelPath) {
        this.modelPath = modelPath;
        try {
            this.model = new Model(modelPath);
            this.recognizer = new Recognizer(model, 16000);;
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
}
