package com.oscarrtorres.openmodelfx.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoskModel {
    private String zipUrl;
    private String name;
    private String language;

    public VoskModel(String zipUrl, String language) {
        this.setZipUrl(zipUrl);
        this.language = language;
    }

    public void setZipUrl(String zipUrl) {
        this.zipUrl = zipUrl;

        int lastSlashIndex = zipUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < zipUrl.length() - 1) {
            this.name = zipUrl.substring(lastSlashIndex + 1); // https://web.com/a.zip -> a.zip
            this.name = this.name.substring(0, this.name.length() - 4); // a.zip -> a
        }
    }
}
