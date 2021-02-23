package com.codewithmab.classroomclient;

import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A java Class to Model each Announcement in a given Google Classroom Course
 */
public class CourseDetailsItem{
    private final String announcementId;
    private final String message;
    private final Long time;
    private final String author;
    private List<Material> materials;

    public CourseDetailsItem(String message, String time, String author,List<Material> materials, String announcementId){
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
}