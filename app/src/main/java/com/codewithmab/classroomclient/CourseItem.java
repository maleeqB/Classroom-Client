package com.codewithmab.classroomclient;

/**
 * A java Class to Model each Course for a given Account
 */
public class CourseItem {
    private final String title;
    private final String section;
    private String id;

    public CourseItem(String title, String section, String id){
        this.title = title;
        this.section = section;
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getSection() {
        return section;
    }

}