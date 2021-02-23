package com.codewithmab.classroomclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.classroom.ClassroomScopes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 1;
    private static final String NEW_USER = "NewUser";

    //Google Classroom API Scopes required by the App
    private static final Scope[] SCOPES = {
            new Scope(ClassroomScopes.CLASSROOM_ANNOUNCEMENTS),
            //new Scope(ClassroomScopes.CLASSROOM_ANNOUNCEMENTS_READONLY),
            new Scope(ClassroomScopes.CLASSROOM_COURSES_READONLY),
            new Scope(ClassroomScopes.CLASSROOM_STUDENT_SUBMISSIONS_ME_READONLY),

            new Scope(ClassroomScopes.CLASSROOM_PROFILE_EMAILS),
            new Scope(ClassroomScopes.CLASSROOM_ROSTERS),
            new Scope(ClassroomScopes.CLASSROOM_ROSTERS_READONLY),
            new Scope(ClassroomScopes.CLASSROOM_PROFILE_PHOTOS),


            //new Scope(ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS_READONLY),
            //new Scope(ClassroomScopes.CLASSROOM_COURSEWORK_ME_READONLY),
            new Scope(ClassroomScopes.CLASSROOM_COURSEWORK_ME),
            new Scope(ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS)
    };

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sharedPreferences = getApplicationContext().getSharedPreferences(NEW_USER, Context.MODE_PRIVATE);
        //Checks whether the preferences already contains the New_User key
        if(!(sharedPreferences.contains(NEW_USER)))
            displayWelcomeGuide();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        //Requesting a dozen Scopes may seem too overwhelming for the Users,
        // so rather than requesting the Scopes now Here I rely on the EAFP rule
        //This is merely a design choice, you can choose to request the Scopes by Un-Commenting the requestScopes() line below
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestScopes(new Scope(ClassroomScopes.CLASSROOM_COURSES), SCOPES)
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Check if user is already signed into the App
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        //check if user already signed in and move to Courses Activity
        if(account != null){
            startActivity(new Intent(SignInActivity.this, CoursesActivity.class));
            finish();
        }

        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    private void displayWelcomeGuide(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml(getString(R.string.welcome_message)));
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(NEW_USER,"SignedIn");
            editor.apply();
            dialog.dismiss();
        });

        builder.create().show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Toast.makeText(this,"Sign in Successfully",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, CoursesActivity.class));
            finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            int errorCode = e.getStatusCode();

            Snackbar snackbar = Snackbar.make(findViewById(R.id.sign_in_linearlayout), "Sign-in failed", Snackbar.LENGTH_LONG);
            snackbar.setAction("View Details", v -> {
                String message = CommonStatusCodes.getStatusCodeString(errorCode);

                new AlertDialog.Builder(this).setMessage(message).create().show();
            });
            snackbar.show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button)
            signIn();

    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}