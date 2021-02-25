package com.codewithmab.classroomclient;

import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * A java Class to Model each Announcement in a given Google Classroom Course
 */
public class CourseDetailsItem implements Comparable<CourseDetailsItem>{
    private final String announcementId;
    private final String message;
    private final Long time;
    private final String author;
    private List<Material> materials;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseDetailsItem that = (CourseDetailsItem) o;
        return Objects.equals(announcementId, that.announcementId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(time, that.time) &&
                Objects.equals(author, that.author) &&
                Objects.equals(materials, that.materials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(announcementId, message, time, author, materials);
    }

    public CourseDetailsItem(String message, String time, String author, List<Material> materials, String announcementId){
        this.message = message;
        this.time = getLongTime(time);
        this.author = author;
        this.materials = materials;
        this.announcementId = announcementId;
    }
    public CourseDetailsItem(String message, Long time, String author,List<Material> materials, String announcementId){
        this.message = message;
        this.time = time;
        this.author = author;
        this.materials = materials;
        this.announcementId = announcementId;
    }

    public String getAnnouncementId(){
        return announcementId;
    }
    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public Long getTime(){
        return time;
    }

    private Long getLongTime(String time) {
        DateTime dateTime = DateTime.parseRfc3339(time);
        return dateTime.getValue();
    }

    public String getFormattedTime()  {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());
        if(new Date().getTime() - 86400000 < time)
            dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        else if(new Date().getTime() - 604800000 < time)
            dateFormat = new SimpleDateFormat("E, h:mm a", Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(time));
    }

    @Override
    public int compareTo(CourseDetailsItem o) {
        return this.getTime().compareTo(o.getTime());
    }
}