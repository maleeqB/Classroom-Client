package com.codewithmab.classroomclient;

import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A java Class to Model each CourseWork in a given Google Classroom Course
 */
public class CourseWorkItem {
    private final String id;
    private final String title;
    private final Long creationTime;
    private final String description;
    private final String dueDate;

    public CourseWorkItem(String id, String creationTime, String title, String description, com.google.api.services.classroom.model.Date dueDate){
        this.id = id;
        this.title = title;
        this.creationTime = getLongTime(creationTime);
        this.description = description;
        this.dueDate = getStringDate(dueDate);
    }
    public CourseWorkItem(String id, Long creationTime, String title, String description, String dueDate){
        this.id = id;
        this.title = title;
        this.creationTime = creationTime;
        this.description = description;
        this.dueDate = dueDate;
    }
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Long getCreationTime() {
        return creationTime;
    }
    public String getFormattedCreationTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());
        if(new Date().getTime() - 86400000 < creationTime)
            dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        else if(new Date().getTime() - 604800000 < creationTime)
            dateFormat = new SimpleDateFormat("E, h:mm a", Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(creationTime));
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    private Long getLongTime(String time) {
        DateTime dateTime = DateTime.parseRfc3339(time);
        return dateTime.getValue();
    }

    private String getStringDate(com.google.api.services.classroom.model.Date date){
        if(date == null)
            return "No due date";
        try {
            String dateStr = date.getYear() + " " + date.getMonth() + " " + date.getDay();
            Date nDate = new SimpleDateFormat("yyyy M d", Locale.getDefault()).parse(dateStr);
            return new SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(nDate);
        } catch (ParseException e) {
            return "invalid date";
        }

    }

}
