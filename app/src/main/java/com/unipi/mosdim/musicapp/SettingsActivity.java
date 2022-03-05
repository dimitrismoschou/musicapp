package com.unipi.mosdim.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    Button buttonLogout;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.buttonLogout);

    }
    public void logout(View v){
        mAuth.signOut();
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
        MainActivity.mediaPlayer.reset();
        finish();
    }
}