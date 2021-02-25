package com.codewithmab.classroomclient;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(label, material.label) &&
                Objects.equals(link, material.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, link);
    }
}