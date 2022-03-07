package com.unipi.mosdim.musicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    Button buttonLogout;
    FirebaseAuth mAuth;
    Switch switch1;
    public static Boolean switcher=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.buttonLogout);
        switch1 = findViewById(R.id.switch1);
        if(switcher == true) {
            switch1.setChecked(true);
        }
            switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b == true) {
                        MainActivity.mediaPlayer.reset();
                        DatabaseReference myRef;
                        myRef = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("user_pref");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.getKey().equals(MainActivity.uid)) {
                                        MainActivity.preference = snapshot.child("genre").getValue().toString();
                                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                    else
                    {
                        MainActivity.preference = "";
                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                        switcher = false;
                    }
                }
            });
    }
    public void logout(View v){
        mAuth.signOut();
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
        MainActivity.mediaPlayer.reset();
        finish();
//        MainActivity.mediaPlayer.reset();
//        MainActivity.preference = "greek"; MainActivity.uid
//        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
    }

    public void showpref(View view){

    }
}