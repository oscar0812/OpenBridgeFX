package com.oscarrtorres.openmodelfx.models;

import com.knuddels.jtokkit.api.ModelType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class YamlData {
    private ChatGptConfig chatGpt;
    private VoskConfig vosk;

    public YamlData() {
        this.chatGpt = new ChatGptConfig();
        this.vosk = new VoskConfig();
    }

    @Data
    public static class ChatGptConfig {
        private String apiKey;
        private String apiUrl = "https://api.openai.com/v1/chat/completions";
        private String model = ModelType.GPT_4O_MINI.getName();
        private List<ModelPricing> modelList = new ArrayList<>();

        public boolean isValid() {
            return !Objects.isNull(getApiUrl()) && !getApiUrl().isBlank() &&
                    !Objects.isNull(getApiKey()) && !getApiKey().isBlank() &&
                    !Objects.isNull(getModel()) && !getModel().isBlank();
        }
    }

    @Data
    public static class VoskConfig {
        private String model;
        private List<VoskModel> modelList;
    }
}
