package com.unipi.mosdim.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        buttonLogout = findViewById(R.id.buttonLogout);
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        mediaPlayer.reset();
        finish();
    }
}