package com.unipi.mosdim.musicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {
    TextInputEditText edit_email;
    TextInputEditText edit_password;
    Button update_button;

    private FirebaseAuth mAuth;

    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        edit_email = findViewById(R.id.edit_email);
        edit_password = findViewById(R.id.edit_password);
        update_button = findViewById(R.id.update_button);
        update_button.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        edit_password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (edit_password.length()>5){
                    update_button.setEnabled(true);
                }
                else
                    update_button.setEnabled(false);
                return false;
            }
        });
        update_button.setOnClickListener(view -> {
            updateUser();
        });



    }

    private void updateUser() {
        String email = edit_email.getText().toString();
        String password = edit_password.getText().toString();


        if (TextUtils.isEmpty(email)){
            edit_email.setError("Email cannot be empty");
            edit_email.requestFocus();
        }
        else if (TextUtils.isEmpty(password)){
            edit_password.setError("Password cannot be empty");
            edit_password.requestFocus();
        }
        else {
            user.updateEmail(email);
            user.updatePassword(password);
            mAuth.updateCurrentUser(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(UserProfile.this, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserProfile.this, MainActivity.class));
                    }
                    else {
                        Toast.makeText(UserProfile.this, "Update Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public void onStart () {
        super.onStart();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(UserProfile.this, LoginActivity.class));
        }
        edit_email.setText(user.getEmail());
    }
}