package com.oscarrtorres.openbridgefx.models;

import java.util.Objects;

public class EnvData {
    private String apiKey;
    private String apiUrl;
    private String model;

    public EnvData(String apiKey, String apiUrl, String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isValid() {
        return !Objects.isNull(getApiUrl()) && !getApiUrl().isBlank() &&
                !Objects.isNull(getApiKey()) && !getApiKey().isBlank() &&
                !Objects.isNull(getModel()) && !getModel().isBlank();
    }

    @Override
    public String toString() {
        return "EnvData{" +
                "apiKey='" + apiKey + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}

