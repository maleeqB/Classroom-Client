package com.codewithmab.classroomclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithmab.classroomclient.Db.DbHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.ListCoursesResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoursesActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    FetchCoursesThread fetchCoursesThread = new FetchCoursesThread("FetchCoursesThread", this);

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

    LinearLayoutManager lin;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    FrameLayout progressBarHolder;
    RelativeLayout relativeLayout;
    RecyclerView recyclerView;
    List<CourseItem> courseItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        setTitle("Classroom Courses");
        courseItems = new ArrayList<>();
        progressBarHolder = findViewById(R.id.progressBarHolder);
        relativeLayout = findViewById(R.id.courses_relativelayout);

        recyclerView = findViewById(R.id.courses_recyclerView);
        lin=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lin);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!fetchCoursesThread.isAlive()) {
            fetchCoursesThread = new FetchCoursesThread("FetchCoursesThread", this);


            fetchCoursesThread.start();

            popupLayout();

            Handler fetchCourses = new Handler(fetchCoursesThread.getLooper());
            //The Work to be Performed by the Background thread
            //this Goes like an Infinite for-loop unless stopBackgroundWork() is called
            //stopped when user navigates away from the App
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    CoursesActivity coursesActivity = fetchCoursesThread.activityWeakReference.get();
                    if (coursesActivity == null || coursesActivity.isFinishing())
                        return;
                    runOnUiThread(coursesActivity::putDialog);

                    if (!(isInternetWorking())) {
                        runOnUiThread(() -> coursesActivity.showMsg("No Internet Connection ..."));
                        runOnUiThread(coursesActivity::popupLayout);
                        fetchCourses.postDelayed(this, 1000 * 30);
                        return;
                    }

                    DbHelper databaseHelper = DbHelper.getInstance(coursesActivity);
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(coursesActivity);
                    if (account == null)
                        return;
                    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(coursesActivity, Arrays.asList(SCOPES));
                    credential.setSelectedAccountName(account.getDisplayName());
                    credential.setSelectedAccount(account.getAccount());


                    Classroom service = new Classroom.Builder(AndroidHttp.newCompatibleTransport(), jsonFactory, credential)
                            .setApplicationName("Classroom")
                            .build();


                    String pageToken = null;
                    boolean isUpdated = false;
                    try {
                        do {
                            if (isUpdated)
                                break;
                            ListCoursesResponse response = service.courses().list()
                                    .setPageSize(10)
                                    .setPageToken(pageToken)
                                    .execute();
                            List<Course> mCourses = response.getCourses();
                            if (mCourses == null || mCourses.size() == 0)
                                break;
                            else {
                                for (Course course : response.getCourses()) {
                                    String title = course.getName();
                                    String section = course.getSection();
                                    String id = course.getId();
                                    CourseItem courseItem = new CourseItem(title, section, id);
                                    if (!(databaseHelper.addCourseItem(courseItem)))
                                        isUpdated = true;

                                }
                                runOnUiThread(coursesActivity::popupLayout);

                            }
                            pageToken = response.getNextPageToken();

                        } while (pageToken != null);
                    } catch (UserRecoverableAuthIOException e) {
                        stopBackgroundWork();
                        startActivityForResult(e.getIntent(), RC_SIGN_IN);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(coursesActivity::removeDialog);
                    fetchCourses.postDelayed(this, 1000 * 30);
                }
            };
            fetchCourses.post(runnable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            onStart();
        }
    }

    public void showMsg(String message){
        Snackbar.make(findViewById(R.id.courses_relativelayout), message, Snackbar.LENGTH_LONG).show();
    }
    public void popupLayout(){
        List<CourseItem> courseItems = DbHelper.getInstance(this).getAllCourseItem();
        if(this.courseItems.size() == courseItems.size())
            return;
        this.courseItems = courseItems;
        recyclerView.setAdapter(new CoursesAdapter(this, this.courseItems));
        showMsg("Fetching new Courses ...");

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

    public void stopBackgroundWork(){
        fetchCoursesThread.quitSafely();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundWork();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings){
            stopBackgroundWork();
            startActivity(new Intent(CoursesActivity.this, SettingsActivity.class));
        } else if(item.getItemId() == R.id.help){
            stopBackgroundWork();
            startActivity(new Intent(CoursesActivity.this, HelpActivity.class));
        } else if(item.getItemId() == R.id.about){
            stopBackgroundWork();
            startActivity(new Intent(CoursesActivity.this, AboutActivity.class));
        }
        return true;
    }

}

/**
 * Background thread that handles Fetching of the User Courses for each Given Account
 */
class FetchCoursesThread extends HandlerThread {
    WeakReference<CoursesActivity> activityWeakReference;

    public FetchCoursesThread(String threadName, CoursesActivity coursesActivity){
        super(threadName);
        activityWeakReference = new WeakReference<>(coursesActivity);
    }

}

class CoursesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    CoursesActivity coursesActivity;
    List<CourseItem> li;

    public CoursesAdapter(CoursesActivity coursesActivity, List<CourseItem> li){
        this.coursesActivity = coursesActivity;
        this.li=li;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.courses_layout, parent, false);
        return new CourseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CourseViewHolder) holder).title.setText(li.get(position).getTitle());
        ((CourseViewHolder) holder).section.setText(li.get(position).getSection());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CourseDetailsActivity.class);
            intent.putExtra("id",li.get(position).getId());
            intent.putExtra("title",li.get(position).getTitle());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return li.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder{

        TextView title,section;

        public CourseViewHolder(View v){
            super(v);

            title=v.findViewById(R.id.course_title);
            section=v.findViewById(R.id.course_section);

        }

    }

}

