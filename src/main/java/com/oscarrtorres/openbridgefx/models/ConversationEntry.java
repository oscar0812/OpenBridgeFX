package com.oscarrtorres.openbridgefx.models;

import java.util.HashMap;
import java.util.Map;

public class ConversationEntry {
    private String timestamp;
    private String rawPrompt;
    private String response;
    private String finalPrompt;
    private Map<String, String> parameters;

    private boolean loadedFromJson;

    private TokenCostInfo promptInfo;
    private TokenCostInfo responseInfo;

    public ConversationEntry() {
        this.timestamp = null;
        this.rawPrompt = "";
        this.response = "";
        this.finalPrompt = "";
        this.parameters = new HashMap<>();
    }

    public ConversationEntry(String timestamp, String rawPrompt, String response, String finalPrompt,
                             Map<String, String> parameters, boolean loadedFromJson, TokenCostInfo promptTokenInfo, TokenCostInfo responseTokenInfo) {
        this.timestamp = timestamp;
        this.rawPrompt = rawPrompt;
        this.response = response;
        this.finalPrompt = finalPrompt;
        this.parameters = parameters;
        this.loadedFromJson = loadedFromJson;
        this.promptInfo = promptTokenInfo;
        this.responseInfo = responseTokenInfo;
    }
    // Getters and Setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRawPrompt() {
        return rawPrompt;
    }

    public void setRawPrompt(String rawPrompt) {
        this.rawPrompt = rawPrompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getFinalPrompt() {
        return finalPrompt;
    }

    public void setFinalPrompt(String finalPrompt) {
        this.finalPrompt = finalPrompt;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isLoadedFromJson() {
        return loadedFromJson;
    }

    public void setPromptInfo(TokenCostInfo promptInfo) {
        this.promptInfo = promptInfo;
    }

    public TokenCostInfo getPromptInfo() {
        return promptInfo;
    }

    public void setResponseInfo(TokenCostInfo responseInfo) {
        this.responseInfo = responseInfo;
    }

    public TokenCostInfo getResponseInfo() {
        return responseInfo;
    }
}

