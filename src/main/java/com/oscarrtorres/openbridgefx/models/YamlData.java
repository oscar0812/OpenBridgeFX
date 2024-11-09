package com.oscarrtorres.openbridgefx.models;

import com.knuddels.jtokkit.api.ModelType;

import java.util.List;
import java.util.Objects;

public class YamlData {
    private ChatGptConfig chatGpt;
    private VoskConfig vosk;

    public YamlData() {
        this.chatGpt = new ChatGptConfig();
        this.vosk = new VoskConfig();
    }

    public ChatGptConfig getChatGpt() {
        return chatGpt;
    }

    public void setChatGpt(ChatGptConfig chatGpt) {
        this.chatGpt = chatGpt;
    }

    public VoskConfig getVosk() {
        return vosk;
    }

    public void setVosk(VoskConfig vosk) {
        this.vosk = vosk;
    }

    public static class ChatGptConfig {
        private String apiKey;
        private String apiUrl = "https://api.openai.com/v1/chat/completions";
        private String model = ModelType.GPT_4O_MINI.getName();

        public boolean isValid() {
            return !Objects.isNull(getApiUrl()) && !getApiUrl().isBlank() &&
                    !Objects.isNull(getApiKey()) && !getApiKey().isBlank() &&
                    !Objects.isNull(getModel()) && !getModel().isBlank();
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
    }

    public static class VoskConfig {
        private String model;
        private List<String> modelList;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<String> getModelList() {
            return modelList;
        }

        public void setModelList(List<String> modelList) {
            this.modelList = modelList;
        }
    }

    @Override
    public String toString() {
        return "YamlData{" +
                "chatGpt=" + chatGpt +
                ", vosk=" + vosk +
                '}';
    }
}
