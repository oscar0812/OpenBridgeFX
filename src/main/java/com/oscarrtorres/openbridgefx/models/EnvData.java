package com.oscarrtorres.openbridgefx.models;

import java.util.Objects;

public class EnvData {
    private String apiKey;
    private String apiUrl;
    private String model;
    private String voskModel;

    public EnvData() {
        this.apiKey = null;
        this.apiUrl = null;
        this.model = null;
        this.voskModel = null;
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

    public boolean hasValidApiData() {
        return !Objects.isNull(getApiUrl()) && !getApiUrl().isBlank() &&
                !Objects.isNull(getApiKey()) && !getApiKey().isBlank() &&
                !Objects.isNull(getModel()) && !getModel().isBlank();
    }

    public void setVoskModel(String voskModel) {
        this.voskModel = voskModel;
    }

    public String getVoskModel() {
        return voskModel;
    }

    @Override
    public String toString() {
        return "EnvData{" +
                "apiKey='" + apiKey + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", model='" + model + '\'' +
                ", voskModel='" + voskModel + '\'' +
                '}';
    }
}

