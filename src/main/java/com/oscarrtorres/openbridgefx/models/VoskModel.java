package com.oscarrtorres.openbridgefx.models;

public class VoskModel {
    private String zipUrl;
    private String name;
    private String language;

    public VoskModel() {

    }

    public VoskModel(String zipUrl, String language) {
        this.setZipUrl(zipUrl);
        this.language = language;
    }

    public String getZipUrl() {
        return zipUrl;
    }

    public void setZipUrl(String zipUrl) {
        this.zipUrl = zipUrl;

        int lastSlashIndex = zipUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < zipUrl.length() - 1) {
            this.name = zipUrl.substring(lastSlashIndex + 1); // https://web.com/a.zip -> a.zip
            this.name = this.name.substring(0, this.name.length() - 4); // a.zip -> a
        }
    }

    public String getName() {
        return this.name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
