package com.codewithmab.classroomclient;

/**
 * A java class to Model Materials Associated with a Classroom Course Announcement
 *
 * For a given Course announcement there is a 0..n Material Attached
 */
public class Material{
    private final String label;
    private final String link;
    public Material(String label, String link){
        this.label = label;
        this.link = link;
    }
    public String getLink(){
        return link;
    }
    public String getLabel(){
        return label;
    }
}