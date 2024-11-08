package com.oscarrtorres.openbridgefx.models;

import java.util.List;
import java.util.Objects;

public class YamlData {
    private String apiKey;
    private String apiUrl;
    private String chatGptModel;
    private String voskModel;

    private List<String> voskModelList;

    public YamlData() {
        this.apiKey = null;
        this.apiUrl = null;
        this.chatGptModel = null;
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

    public String getChatGptModel() {
        return chatGptModel;
    }

    public void setChatGptModel(String chatGptModel) {
        this.chatGptModel = chatGptModel;
    }

    public boolean hasValidApiData() {
        return !Objects.isNull(getApiUrl()) && !getApiUrl().isBlank() &&
                !Objects.isNull(getApiKey()) && !getApiKey().isBlank() &&
                !Objects.isNull(getChatGptModel()) && !getChatGptModel().isBlank();
    }

    public void setVoskModel(String voskModel) {
        this.voskModel = voskModel;
    }

    public String getVoskModel() {
        return voskModel;
    }

    public void setVoskModelList(List<String> voskModelList) {
        this.voskModelList = voskModelList;
    }

    public List<String> getVoskModelList() {
        return voskModelList;
    }

    @Override
    public String toString() {
        return "EnvData{" +
                "apiKey='" + apiKey + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", model='" + chatGptModel + '\'' +
                ", voskModel='" + voskModel + '\'' +
                '}';
    }
}

