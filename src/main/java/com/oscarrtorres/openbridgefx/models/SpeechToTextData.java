package com.oscarrtorres.openbridgefx.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeechToTextData {
    private String modelPath;
    private String modelName;
    private Model model;
    private Recognizer recognizer;

    public SpeechToTextData(String modelPath) {
        this.modelPath = modelPath;
        this.modelName = modelPath.substring(modelPath.lastIndexOf('/') + 1);
    }

    public boolean isLoaded() {
        return !Objects.isNull(this.getModel());
    }
}
