package com.microsoft.cognitiveservices.speech.project.recApp.models;

public class Transmodel {
    private String filename;
    private String url;
    private String unique_id;

    public Transmodel() {
    }

    public Transmodel(String filename, String url,String uid) {
        this.filename = filename;
        this.url = url;
        this.setUnique_id(uid);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public void setUnique_id(String unique_id) {
        this.unique_id = unique_id;
    }
}
