package com.codewithmab.classroomclient;

/**
 * A java Class to Model each User Profile for a given Google Classroom Course
 */
public class UserProfileItem {
    public enum UserType{Teacher, Student}

    private final UserType userType;
    private final String name;
    private final String userId;

    public UserProfileItem(UserType userType, String name, String userId){
        this.userType = userType;
        this.name = name;
        this.userId = userId;
    }

    public String getUserType() {
        return userType.name();
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }
}
