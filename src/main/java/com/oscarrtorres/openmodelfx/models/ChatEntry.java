package com.oscarrtorres.openmodelfx.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatEntry {
    private String timestamp;
    private String rawPrompt;
    private String response;
    private String finalPrompt;
    private String modelName;
    private Map<String, String> parameters;

    private boolean loadedFromJson;

    private TokenCostInfo promptInfo;
    private TokenCostInfo responseInfo;
}

