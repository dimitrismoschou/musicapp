package com.unipi.mosdim.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GenresActivity extends AppCompatActivity {

    Button readyButton;
    Spinner genresSpinner;
    DatabaseReference myRef;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genres);

        genresSpinner = findViewById(R.id.spinner);



        readyButton = findViewById(R.id.readybutton);


        Bundle extras = getIntent().getExtras();

         readyButton.setOnClickListener(view -> {
             FirebaseDatabase db = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/");
             myRef = db.getReference().child("user_pref");
             DatabaseReference ref1 = myRef.child(extras.getString("uid"));
             DatabaseReference ref2 = ref1.child("genre");
             ref2.setValue(genresSpinner.getSelectedItem().toString());
             startActivity(new Intent(GenresActivity.this, MainActivity.class));
             finish();
         });

    }
}