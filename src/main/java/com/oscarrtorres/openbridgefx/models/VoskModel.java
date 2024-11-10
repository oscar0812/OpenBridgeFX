package com.oscarrtorres.openbridgefx.models;

public class VoskModel {
    private final String zipUrl;
    private String name;

    public VoskModel(String zipUrl) {
        this.zipUrl = zipUrl;
        int lastSlashIndex = zipUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < zipUrl.length() - 1) {
            this.name = zipUrl.substring(lastSlashIndex + 1); // https://web.com/a.zip -> a.zip
            this.name = this.name.substring(0, this.name.length() - 4); // a.zip -> a
        }
    }

    public String getZipUrl() {
        return zipUrl;
    }

    public String getName() {
        return name;
    }
}
