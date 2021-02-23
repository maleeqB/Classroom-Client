package com.codewithmab.classroomclient;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codewithmab.classroomclient.Db.DbHelper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SettingsActivity extends AppCompatActivity {

    Button signOutButton, revokeAccessButton;
    TextView accountName, accountMail;
    GoogleSignInClient mGoogleSignInClient;
    DbHelper dbHelper;

    AlphaAnimation inAnimation;

    FrameLayout progressBarHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progressBarHolder = findViewById(R.id.progressBarHolder);

        signOutButton = findViewById(R.id.sign_out_button);
        revokeAccessButton = findViewById(R.id.revoke_access_button);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        accountName = findViewById(R.id.account_name);
        accountMail = findViewById(R.id.account_email);

        accountName.setText("Name : "+account.getDisplayName());
        accountMail.setText("E-Mail : "+account.getEmail());
        dbHelper = DbHelper.getInstance(this);


        signOutButton.setOnClickListener(v -> {
            confirmSignOut(0);
        });

        revokeAccessButton.setOnClickListener(v ->{
            confirmSignOut(1);

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void confirmSignOut(int type){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml(getString(R.string.database_caution)));
        //builder.setCancelable(false);
        builder.setPositiveButton("YES", (dialog, which) -> {
            disableButtons();
            putDialog();
            if(type == 0)
                signOut();
            else
                revokeAccess();
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();

    }

    public void disableButtons(){
        signOutButton.setEnabled(false);
        revokeAccessButton.setEnabled(false);
    }
    public void putDialog(){
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);
    }
    private void signOut(){
        dbHelper.resetDatabase();
        Auth.GoogleSignInApi.signOut(mGoogleSignInClient.asGoogleApiClient()).setResultCallback(
                status -> {
                    // Signed out successfully, update UI.
                    Toast.makeText(getApplicationContext(),"Signed out Successfully",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
                    finish();
                }
        );
    }
    private void revokeAccess(){
        dbHelper.resetDatabase();
        Auth.GoogleSignInApi.revokeAccess(mGoogleSignInClient.asGoogleApiClient()).setResultCallback(
                status -> {
                    Toast.makeText(getApplicationContext(),"Access revoked Successfully",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
                    finish();
                }
        );
    }
}