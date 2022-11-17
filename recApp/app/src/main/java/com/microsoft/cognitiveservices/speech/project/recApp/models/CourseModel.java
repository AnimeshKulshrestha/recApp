package com.microsoft.cognitiveservices.speech.project.recApp.models;

public class CourseModel {
    public CourseModel() {
    }

    String course , pushId;

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    User instructor;

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }



    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public CourseModel(String course , User instructor, String pushId){
        this.pushId = pushId;
        this.course = course;
        this.instructor = instructor;
    }
}
