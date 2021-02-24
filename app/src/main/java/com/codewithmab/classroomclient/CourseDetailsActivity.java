package com.codewithmab.classroomclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codewithmab.classroomclient.Db.DbHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.Announcement;
import com.google.api.services.classroom.model.CourseWork;
import com.google.api.services.classroom.model.ListAnnouncementsResponse;
import com.google.api.services.classroom.model.ListCourseWorkResponse;
import com.google.api.services.classroom.model.ListStudentsResponse;
import com.google.api.services.classroom.model.ListTeachersResponse;
import com.google.api.services.classroom.model.Student;
import com.google.api.services.classroom.model.Teacher;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDetailsActivity extends AppCompatActivity {
    FetchCourseDetailsThread fetchCourseDetailsThread = new FetchCourseDetailsThread("FetchCourseDetailsThread", this);

    String courseId;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;
    BottomNavigationView bottomNavigationView;

    //a list of all the fragments to be displayed by this activity
    //initialized in onCreate, not using List#of as it's supported only in newer versions of Android
    List<Fragment> fragmentList = new ArrayList<>(); //List.of(new AnnouncementsFragment(), new CourseWorksFragment(), new UserProfilesFragment());


    final String[] SCOPES = {
            ClassroomScopes.CLASSROOM_COURSES,
            ClassroomScopes.CLASSROOM_ANNOUNCEMENTS,
            ClassroomScopes.CLASSROOM_ANNOUNCEMENTS_READONLY,
            ClassroomScopes.CLASSROOM_COURSES_READONLY,
            ClassroomScopes.CLASSROOM_STUDENT_SUBMISSIONS_ME_READONLY,

            ClassroomScopes.CLASSROOM_PROFILE_EMAILS,
            ClassroomScopes.CLASSROOM_ROSTERS,
            ClassroomScopes.CLASSROOM_ROSTERS_READONLY,
            ClassroomScopes.CLASSROOM_PROFILE_PHOTOS,


            ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS_READONLY,
            ClassroomScopes.CLASSROOM_COURSEWORK_ME_READONLY,
            ClassroomScopes.CLASSROOM_COURSEWORK_ME,
            ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS
    };

    //The current Fragment being displayed to the User
    enum CurrentFragment{Announcements, CourseWork, People}

    //keep track of the displayed fragment
    CurrentFragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        courseId = getIntent().getStringExtra("id");
        setTitle(getIntent().getStringExtra("title"));

        fragmentList.addAll(Arrays.asList(new AnnouncementsFragment(), new CourseWorksFragment(), new UserProfilesFragment()));
        for (Fragment f: fragmentList) {
            Bundle bundle = new Bundle();
            bundle.putString("courseId", courseId);
            f.setArguments(bundle);
        }
        fetchCourseDetailsThread.start();

        loadFragment(fragmentList.get(0));
        currentFragment = CurrentFragment.Announcements;
        //popupLayout();

        progressBarHolder = findViewById(R.id.progressBarHolder);
        bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.stream) {
                if(!(currentFragment == CurrentFragment.Announcements)){
                    loadFragment(fragmentList.get(0));
                    currentFragment = CurrentFragment.Announcements;
                }
            } else if(item.getItemId() == R.id.classwork) {
                if(!(currentFragment == CurrentFragment.CourseWork)){
                    loadFragment(fragmentList.get(1));
                    currentFragment = CurrentFragment.CourseWork;
                }
            } else if(item.getItemId() == R.id.people){
                if(!(currentFragment == CurrentFragment.People)){
                    loadFragment(fragmentList.get(2));
                    currentFragment = CurrentFragment.People;
                }
            }
            return true;
        });

        Handler courseDetailsHandler = new Handler(fetchCourseDetailsThread.getLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CourseDetailsActivity activity = fetchCourseDetailsThread.activityWeakReference.get();

                runOnUiThread(activity::putDialog);
                if(!(isInternetWorking())){
                    runOnUiThread(()->activity.showMsg("No Internet Connection ..."));
                    runOnUiThread(activity::popupLayout);
                    courseDetailsHandler.postDelayed(this, 1000*30);
                    return;
                }
                DbHelper databaseHelper = DbHelper.getInstance(activity);
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
                if(account == null)
                    return;
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(activity, Arrays.asList(SCOPES));
                credential.setSelectedAccountName(account.getDisplayName());
                credential.setSelectedAccount(account.getAccount());

                Classroom service = new Classroom.Builder(AndroidHttp.newCompatibleTransport(), jsonFactory,credential)
                        .setApplicationName("Classroom")
                        .build();


                String pageToken = null;
                boolean isAnnouncementUpdated = false;
                try{

                    ListTeachersResponse teachersResponse = service.courses().teachers().list(courseId).execute();
                    ListStudentsResponse studentsResponse = service.courses().students().list(courseId).execute();
                    ListCourseWorkResponse courseWorkResponse = service.courses().courseWork().list(courseId).execute();

                    List<Teacher> teachers = teachersResponse.getTeachers();
                    if(teachers != null)
                        for(Teacher t:teachers){
                            UserProfileItem teacherProfile = new UserProfileItem(UserProfileItem.UserType.Teacher, t.getProfile().getName().getFullName(), t.getUserId());
                            if(!(databaseHelper.addUserProfile(courseId, teacherProfile)))
                                break;
                        }
                    List<Student> students = studentsResponse.getStudents();
                    if(students != null)
                        for(Student s : students){
                            UserProfileItem teacherProfile = new UserProfileItem(UserProfileItem.UserType.Student, s.getProfile().getName().getFullName(), s.getUserId());
                            if(!(databaseHelper.addUserProfile(courseId, teacherProfile)))
                                break;
                        }

                    List<CourseWork> courseWorks = courseWorkResponse.getCourseWork();
                    if(courseWorks != null)
                        for (CourseWork c : courseWorks){
                            CourseWorkItem courseWorkItem = new CourseWorkItem(c.getId(), c.getCreationTime(), c.getTitle(), c.getDescription(), c.getDueDate());
                            if(!(databaseHelper.addCourseWorkItem(courseId, courseWorkItem)))
                                break;
                        }


                    Map<String, String> userProfiles = new HashMap<>();

                    List<UserProfileItem> userProfileItems = databaseHelper.getAllUsersProfile(courseId);
                    for (UserProfileItem userProfileItem:userProfileItems)
                        userProfiles.put(userProfileItem.getUserId(), userProfileItem.getName());


                    do {
                        if(isAnnouncementUpdated)
                            break;
                        ListAnnouncementsResponse response = service.courses().announcements().list(courseId)
                                .setPageSize(200)
                                .setPageToken(pageToken)
                                .execute();
                        List<Announcement> mAnnouncements = response.getAnnouncements();
                        if(mAnnouncements == null || mAnnouncements.size() == 0)
                            break;
                        else{
                            for(Announcement announcement : response.getAnnouncements()){
                                String message = announcement.getText();
                                String time = announcement.getCreationTime();
                                String author = announcement.getCreatorUserId();
                                List<Material> materials = new ArrayList<>();
                                String announcementId = announcement.getId();

                                if(announcement.getMaterials() != null)
                                    for(com.google.api.services.classroom.model.Material material : announcement.getMaterials()){
                                        if(material.getDriveFile() != null){
                                            materials.add(new Material(material.getDriveFile().getDriveFile().getTitle(),material.getDriveFile().getDriveFile().getAlternateLink()));
                                        }
                                        if(material.getYoutubeVideo() != null){
                                            materials.add(new Material(material.getYoutubeVideo().getTitle(), material.getYoutubeVideo().getAlternateLink()));
                                        }
                                        if(material.getForm() != null){
                                            materials.add(new Material(material.getForm().getTitle(), material.getForm().getFormUrl()));
                                        }
                                        if(material.getLink() != null){
                                            materials.add(new Material(material.getLink().getTitle(), material.getLink().getUrl()));
                                        }
                                    }


                                if(userProfiles.containsKey(author))
                                    author = userProfiles.get(author);
                                else author = "User - "+author;

                                CourseDetailsItem item = new CourseDetailsItem(message, time, author, materials, announcementId);
                                if (!(databaseHelper.addCourseDetailsItem(courseId, item)))
                                    isAnnouncementUpdated = true;

                            }
                            pageToken = response.getNextPageToken();
                        }
                        runOnUiThread(activity::popupLayout);
                    } while (pageToken != null);

                } catch (IOException e) { e.printStackTrace();}
                runOnUiThread(activity::removeDialog);
                courseDetailsHandler.postDelayed(this, 1000*30);
            }
        };
        courseDetailsHandler.post(runnable);
    }


    private void loadFragment(Fragment fragment){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.courses_details_fragment, fragment);
        transaction.commit();
    }
    public void popupLayout(){
        switch (currentFragment){
            case Announcements:
                ((AnnouncementsFragment) fragmentList.get(0)).popupLayout();
                break;
            case CourseWork:
                ((CourseWorksFragment) fragmentList.get(1)).popupLayout();
                break;
            case People:
                ((UserProfilesFragment) fragmentList.get(2)).popupLayout();
                break;
        }

    }
    public void showMsg(String message){
        Snackbar.make(findViewById(R.id.courses_details_relativelayout), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetchCourseDetailsThread.quitSafely();
    }

    public void putDialog(){
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);
    }
    public void removeDialog(){
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
    }

    public boolean isInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

}

/**
 * Background thread that handles Fetching of Each Course details such as Announcements and CourseWorks
 */
class FetchCourseDetailsThread extends HandlerThread {
    WeakReference<CourseDetailsActivity> activityWeakReference;



    public FetchCourseDetailsThread(String threadName, CourseDetailsActivity coursesActivity){
        super(threadName);
        activityWeakReference = new WeakReference<>(coursesActivity);
    }

}
