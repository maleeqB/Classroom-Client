package com.codewithmab.classroomclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setTitle("Help");
        WebView webView = findViewById(R.id.webview);
        webView.loadUrl("file:///android_asset/help.html");
    }
}