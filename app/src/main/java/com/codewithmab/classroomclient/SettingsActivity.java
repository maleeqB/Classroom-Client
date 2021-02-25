package com.codewithmab.classroomclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
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

    private static final String CARD_COLOR = "card_color";
    Button signOutButton, revokeAccessButton;
    TextView accountName, accountMail;
    GoogleSignInClient mGoogleSignInClient;
    DbHelper dbHelper;

    AlphaAnimation inAnimation;

    FrameLayout progressBarHolder;
    Spinner spinner;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getApplicationContext().getSharedPreferences(CARD_COLOR, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setTitle("Settings");
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

        spinner = findViewById(R.id.card_color_spinner);
        String[] colors = {"White", "Green", "Yellow", "Blue"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colors);
        String selectedItem = sharedPreferences.getString(CARD_COLOR, "White");
        spinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(selectedItem);
        spinner.setSelection(spinnerPosition);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        editor.putString(CARD_COLOR,"White");
                        editor.apply();
                        break;
                    case 1:
                        editor.putString(CARD_COLOR,"Green");
                        editor.apply();
                        break;
                    case 2:
                        editor.putString(CARD_COLOR,"Yellow");
                        editor.apply();
                        break;
                    case 3:
                        editor.putString(CARD_COLOR,"Blue");
                        editor.apply();
                        break;


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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