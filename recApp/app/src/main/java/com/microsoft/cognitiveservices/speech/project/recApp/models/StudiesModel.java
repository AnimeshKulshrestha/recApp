package com.microsoft.cognitiveservices.speech.project.recApp.models;

public class StudiesModel {
    private String instructor, Subject_Id;

    public String getInst_id() {
        return instructor;
    }


    public void setInst_id(String inst_id) {
        this.instructor = inst_id;
    }

    public String getSubject_Id() {
        return Subject_Id;
    }

    public void setSubject_Id(String subject_Id) {
        this.Subject_Id = subject_Id;
    }

    public StudiesModel() {
    }

    public StudiesModel(String inst_id, String Subject_Id) {
        this.instructor = inst_id;
        this.Subject_Id = Subject_Id;
    }
}
