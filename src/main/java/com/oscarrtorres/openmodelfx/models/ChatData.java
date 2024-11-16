package com.oscarrtorres.openmodelfx.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatData {
    private String fileName;
    private ObservableList<ChatEntry> chatEntries;
    private double totalCharge;
    private String timestamp;

    public ChatData(String fileName) {
        this.fileName = fileName;
        this.chatEntries = FXCollections.observableArrayList();
    }

    public void addChatEntry(ChatEntry chatEntry) {
        chatEntries.add(chatEntry);
        this.totalCharge += chatEntry.getPromptInfo().getTotalCost() + chatEntry.getResponseInfo().getTotalCost();
        this.timestamp = chatEntry.getTimestamp();
    }

    public ChatEntry getLastChatEntry() {
        return this.chatEntries.get(chatEntries.size()-1);
    }

}

