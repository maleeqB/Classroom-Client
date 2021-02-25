package com.codewithmab.classroomclient.Db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.codewithmab.classroomclient.CourseDetailsItem;
import com.codewithmab.classroomclient.CourseItem;
import com.codewithmab.classroomclient.CourseWorkItem;
import com.codewithmab.classroomclient.Material;
import com.codewithmab.classroomclient.UserProfileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper Class for performing all database operations
 * As this a multi-threaded application the database is never closed
 */
public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper mInstance = null;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "courses.db";
    private static final String TABLE_NAME = "student_table";

    private static final String ID = "_id";
    private static final String TITLE = "title";
    private static final String SECTION = "section";
    private static final String COURSE_IDENTITY = "id";

    private static final String MESSAGE = "message";
    private static final String TIME = "time";
    private static final String AUTHOR = "author";
    private static final String ANNOUNCEMENT_IDENTITY = "announcement_id";
    private static final String LABEL = "label";
    private static final String LINK = "link";

    private static final String USER_TYPE = "userType";
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";

    private static final String COURSEWORK_ID = "id";
    private static final String COURSEWORK_CREATION_TIME="creationTime";
    private static final String COURSEWORK_TITLE = "title";
    private static final String COURSEWORK_DESCRIPTION ="description";
    private static final String COURSEWORK_DUE_DATE = "dueDate";

    public static DbHelper getInstance(Context ctx){
        if(mInstance == null){
            mInstance = new DbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +" ("+ ID +" INTEGER PRIMARY KEY,"+ TITLE + " TEXT," + SECTION + " TEXT," + COURSE_IDENTITY + " TEXT "+")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_EXISTING_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DROP_EXISTING_TABLE);
        onCreate(db);
    }

    private void createTables(String courseId){
        String COURSE_TABLE = "_"+courseId;
        String MATERIALS_TABLE = "_"+courseId+"Material";
        String USER_PROFILE_TABLE = "_"+courseId+"UserProfile";
        String COURSEWORK_TABLE = "_"+courseId+"CourseWork";
        SQLiteDatabase db = this.getWritableDatabase();
        String CREATE_COURSE_TABLE = "CREATE TABLE IF NOT EXISTS "+ COURSE_TABLE +" ("+ ID +" INTEGER PRIMARY KEY," + MESSAGE + " TEXT,"+ TIME + " INTEGER," + AUTHOR + " TEXT," + ANNOUNCEMENT_IDENTITY + " TEXT"+")";
        String CREATE_MATERIALS_TABLE = "CREATE TABLE IF NOT EXISTS "+ MATERIALS_TABLE +" (" + ID +" INTEGER PRIMARY KEY," +  LABEL + " TEXT," + LINK + " TEXT," + ANNOUNCEMENT_IDENTITY + " TEXT"+")";
        String CREATE_USER_PROFILE_TABLE = "CREATE TABLE IF NOT EXISTS "+ USER_PROFILE_TABLE +" ("+  ID +" INTEGER PRIMARY KEY," +USER_TYPE +" TEXT,"+USER_NAME+" TEXT,"+USER_ID+" TEXT"+")";
        String CREATE_COURSEWORK_TABLE = "CREATE TABLE IF NOT EXISTS "+ COURSEWORK_TABLE + " ("+ ID +" INTEGER PRIMARY KEY," + COURSEWORK_ID + " TEXT,"+ COURSEWORK_CREATION_TIME + " INTEGER,"+COURSEWORK_TITLE+ " TEXT,"+COURSEWORK_DESCRIPTION+" TEXT,"+COURSEWORK_DUE_DATE+ " TEXT"+")";
        db.execSQL(CREATE_COURSE_TABLE);
        db.execSQL(CREATE_MATERIALS_TABLE);
        db.execSQL(CREATE_USER_PROFILE_TABLE);
        db.execSQL(CREATE_COURSEWORK_TABLE);
    }


    public void resetDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> tables =  new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table';", null);
        cursor.moveToFirst();
        while (!(cursor.isAfterLast())){
            String tableName = cursor.getString(1);
            if(!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence"))
                tables.add(tableName);
            cursor.moveToNext();
        }
        cursor.close();
        for(String tableName : tables){
            db.execSQL("DROP TABLE IF EXISTS "+tableName);
        }
        onCreate(db);
    }

    public boolean addCourseItem(CourseItem courseItem){
        SQLiteDatabase db = this.getWritableDatabase();

        //Check if Item already exists in table
        Cursor c = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+COURSE_IDENTITY+" = '"+courseItem.getId()+"'", null);
        if(c.moveToFirst())
            return false;
        c.close();

        ContentValues values = new ContentValues();
        values.put(TITLE, courseItem.getTitle());
        values.put(SECTION, courseItem.getSection());
        values.put(COURSE_IDENTITY, courseItem.getId());

        db.insert(TABLE_NAME,null,values);
        //db.close();
        return true;
    }

    public List<CourseItem> getAllCourseItem(){
        List<CourseItem> courseItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if(cursor.moveToFirst()){
            do{
                CourseItem courseItem = new CourseItem(cursor.getString(1), cursor.getString(2), cursor.getString(3));
                courseItems.add(courseItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return courseItems;
    }

    public CourseDetailsItem getLatestCourseDetailsItem(String courseId){
        SQLiteDatabase db = this.getWritableDatabase();
        String COURSE_TABLE = "_"+courseId;
        String MATERIALS_TABLE = "_"+courseId+"Material";
        Cursor cursor = db.rawQuery("SELECT * FROM " + COURSE_TABLE + " ORDER BY " + TIME + " DESC", null);

        if(cursor.moveToFirst())
             return new CourseDetailsItem(cursor.getString(1), Long.valueOf(cursor.getString(2)), cursor.getString(3), getMaterials(MATERIALS_TABLE, cursor.getString(4)), cursor.getString(4));
        cursor.close();
        return null;
    }

    public void addCourseDetailsItem(String courseId, CourseDetailsItem courseDetailsItem){
        createTables(courseId);
        String COURSE_TABLE = "_"+courseId;
        String MATERIALS_TABLE = "_"+courseId+"Material";
        SQLiteDatabase db = this.getWritableDatabase();

        //Check if Item already exists in table
        Cursor c = db.rawQuery("SELECT * FROM "+COURSE_TABLE+" WHERE "+ANNOUNCEMENT_IDENTITY+" = '"+courseDetailsItem.getAnnouncementId()+"'", null);
        if(c.moveToFirst())
            return;
        c.close();
        ContentValues values = new ContentValues();
        values.put(MESSAGE, courseDetailsItem.getMessage());
        values.put(TIME, courseDetailsItem.getTime());
        values.put(AUTHOR, courseDetailsItem.getAuthor());
        values.put(ANNOUNCEMENT_IDENTITY, courseDetailsItem.getAnnouncementId());
        db.insert(COURSE_TABLE, null,values);

        for (Material material : courseDetailsItem.getMaterials()) {
            values = new ContentValues();
            values.put(LABEL, material.getLabel());
            values.put(LINK, material.getLink());
            values.put(ANNOUNCEMENT_IDENTITY, courseDetailsItem.getAnnouncementId());
            db.insert(MATERIALS_TABLE, null, values);
        }
    }

    public boolean addCourseWorkItem(String courseId, CourseWorkItem courseWorkItem){
        createTables(courseId);
        String COURSEWORK_TABLE = "_"+courseId+"CourseWork";
        SQLiteDatabase db = this.getWritableDatabase();

        //Check if Item already exists in table
        Cursor c = db.rawQuery("SELECT * FROM "+COURSEWORK_TABLE+" WHERE "+COURSEWORK_ID+" = '"+courseWorkItem.getId()+"'", null);
        if(c.moveToFirst())
            return false;
        c.close();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COURSEWORK_ID, courseWorkItem.getId());
        contentValues.put(COURSEWORK_CREATION_TIME, courseWorkItem.getCreationTime());
        contentValues.put(COURSEWORK_TITLE, courseWorkItem.getTitle());
        contentValues.put(COURSEWORK_DESCRIPTION, courseWorkItem.getDescription());
        contentValues.put(COURSEWORK_DUE_DATE, courseWorkItem.getDueDate());
        db.insert(COURSEWORK_TABLE, null, contentValues);
        //db.close();
        return true;
    }
    public List<CourseWorkItem> getAllCourseWorkItem(String courseId){
        createTables(courseId);
        List<CourseWorkItem> courseWorkItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String COURSEWORK_TABLE = "_"+courseId+"CourseWork";
        Cursor cursor = db.rawQuery("SELECT * FROM " + COURSEWORK_TABLE + " ORDER BY " + COURSEWORK_CREATION_TIME + " DESC", null);
        if(cursor.moveToFirst()){
            do{
                CourseWorkItem courseWorkItem = new CourseWorkItem(cursor.getString(1), Long.valueOf(cursor.getString(2)), cursor.getString(3), cursor.getString(4), cursor.getString(5));
                courseWorkItems.add(courseWorkItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return courseWorkItems;
    }
    public boolean addUserProfile(String courseId, UserProfileItem userProfileItem){
        createTables(courseId);
        String USER_PROFILE_TABLE = "_"+courseId+"UserProfile";
        SQLiteDatabase db = this.getWritableDatabase();

        //Check if Item already exists in table
        Cursor c = db.rawQuery("SELECT * FROM "+USER_PROFILE_TABLE+" WHERE "+USER_ID+" = '"+userProfileItem.getUserId()+"'", null);
        if(c.moveToFirst())
            return false;
        c.close();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_TYPE, userProfileItem.getUserType());
        contentValues.put(USER_NAME, userProfileItem.getName());
        contentValues.put(USER_ID, userProfileItem.getUserId());
        db.insert(USER_PROFILE_TABLE, null, contentValues);
        //db.close();
        return true;
    }

    public List<UserProfileItem> getAllUsersProfile(String courseId){
        createTables(courseId);
        List<UserProfileItem> userProfileItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String USER_PROFILE_TABLE = "_"+courseId+"UserProfile";
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_PROFILE_TABLE, null);
        if(cursor.moveToFirst()){
            do{
                UserProfileItem userProfileItem = new UserProfileItem(UserProfileItem.UserType.valueOf(cursor.getString(1)), cursor.getString(2), cursor.getString(3));
                userProfileItems.add(userProfileItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return userProfileItems;
    }

    public List<UserProfileItem> getTeacherProfiles(String courseId){
        createTables(courseId);
        List<UserProfileItem> teacherProfiles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String USER_PROFILE_TABLE = "_"+courseId+"UserProfile";
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_PROFILE_TABLE + " WHERE "+USER_TYPE+"='Teacher'", null);
        if(cursor.moveToFirst()){
            do{
                UserProfileItem teacherProfileItem = new UserProfileItem(UserProfileItem.UserType.valueOf(cursor.getString(1)), cursor.getString(2), cursor.getString(3));
                teacherProfiles.add(teacherProfileItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return teacherProfiles;
    }

    public List<UserProfileItem> getStudentProfiles(String courseId){
        createTables(courseId);
        List<UserProfileItem> studentProfiles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String USER_PROFILE_TABLE = "_"+courseId+"UserProfile";
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_PROFILE_TABLE + " WHERE "+USER_TYPE+"='Student'", null);
        if(cursor.moveToFirst()){
            do{
                UserProfileItem studentProfileItem = new UserProfileItem(UserProfileItem.UserType.valueOf(cursor.getString(1)), cursor.getString(2), cursor.getString(3));
                studentProfiles.add(studentProfileItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return studentProfiles;
    }

    public List<CourseDetailsItem> getAllCourseDetailsItem(String courseId){
        createTables(courseId);
        List<CourseDetailsItem> courseDetailsItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String COURSE_TABLE = "_"+courseId;
        String MATERIALS_TABLE = "_"+courseId+"Material";
        Cursor cursor = db.rawQuery("SELECT * FROM " + COURSE_TABLE + " ORDER BY " + TIME + " DESC", null);
        if(cursor.moveToFirst()){
            do{
                CourseDetailsItem courseDetailsItem = new CourseDetailsItem(cursor.getString(1), Long.valueOf(cursor.getString(2)), cursor.getString(3), getMaterials(MATERIALS_TABLE, cursor.getString(4)), cursor.getString(4));
                courseDetailsItems.add(courseDetailsItem);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return courseDetailsItems;
    }
    private List<Material> getMaterials(String materialTableName, String announcementId){
        List<Material> materials = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + materialTableName + " WHERE "+ANNOUNCEMENT_IDENTITY+"="+announcementId, null);
        if(cursor.moveToFirst()){
            do{
                Material material = new Material(cursor.getString(1), cursor.getString(2));
                materials.add(material);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return materials;
    }

}