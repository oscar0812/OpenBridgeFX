package com.oscarrtorres.openbridgefx.models;

import java.nio.file.Paths;

public class VoskModel {
    private final String zipUrl;
    private String name;

    public VoskModel(String zipUrl) {
        this.zipUrl = zipUrl;
        int lastSlashIndex = zipUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < zipUrl.length() - 1) {
            this.name = zipUrl.substring(lastSlashIndex + 1);
        }
    }

    public String getZipUrl() {
        return zipUrl;
    }

    public String getName() {
        return name;
    }
}
