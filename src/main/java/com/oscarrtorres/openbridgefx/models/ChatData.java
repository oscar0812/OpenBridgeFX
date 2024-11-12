package com.oscarrtorres.openbridgefx.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatData {
    private String fileName;
    private ObservableList<ChatEntry> chatEntries;
    private double totalCharge;
    private String timestamp;

    public ChatData() {

    }

    public ChatData(String fileName) {
        this.fileName = fileName;
        this.chatEntries = FXCollections.observableArrayList();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setChatEntries(ObservableList<ChatEntry> chatEntries) {
        this.chatEntries = chatEntries;
    }

    public ObservableList<ChatEntry> getChatEntries() {
        return chatEntries;
    }

    public void addChatEntry(ChatEntry chatEntry) {
        chatEntries.add(chatEntry);
        this.totalCharge += chatEntry.getPromptInfo().getTotalCost() + chatEntry.getResponseInfo().getTotalCost();
        this.timestamp = chatEntry.getTimestamp();
    }

    public ChatEntry getLastChatEntry() {
        return this.chatEntries.get(chatEntries.size()-1);
    }

    public double getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(double totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

