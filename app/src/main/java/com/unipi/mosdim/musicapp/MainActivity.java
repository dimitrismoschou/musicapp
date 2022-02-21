package com.unipi.mosdim.musicapp;

import static android.view.Gravity.CENTER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SeekBar seekBar;
    Button btnLogOut;
    FirebaseAuth mAuth;
    LinearLayout layout,l, firstlayout;
    ScrollView scrollView;
    MediaPlayer mediaPlayer = new MediaPlayer();
    int length=0;
    ArrayList<String> songName = new ArrayList<>();
    ArrayList<String> artistName = new ArrayList<>();
    ArrayList<String> category = new ArrayList<>();
    ArrayList<String> link = new ArrayList<>();
    ArrayList<String> location = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = (SeekBar) findViewById(R.id.seekBar3);

        btnLogOut = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();

        btnLogOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
        DatabaseReference myRef;
        myRef = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    songName.add(snapshot.getKey());   //τιτλος βιβλιων
                    artistName.add((String) snapshot.child("artist").getValue());
                    link.add((String) snapshot.child("link").getValue());
                    location.add((String) snapshot.child("location").getValue());
                    category.add((String) snapshot.child("category").getValue());
                    getAllData(i);
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }});

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    private Handler mSeekbarUpdateHandler = new Handler();
    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(this, 50);
        }
    };
    public void getAllData(int i){
        System.out.println(songName);
        //hardcoded components
        layout = findViewById(R.id.layout_parent);
        scrollView = findViewById(R.id.scrollView_parent);

        firstlayout = new LinearLayout(this);
        firstlayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView t1 = new TextView(this);
        t1.setLayoutParams(lparams);
        t1.setText(songName.get(i));

        LinearLayout l1h = new LinearLayout(this);
        l1h.setOrientation(LinearLayout.HORIZONTAL);
        l1h.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        l1h.setGravity(CENTER);

        LinearLayout l1v = new LinearLayout(this);
        l1v.setOrientation(LinearLayout.VERTICAL);

        TextView title1 = new TextView(this);
        title1.setLayoutParams(lparams);
        title1.setText(category.get(i));
        l1v.addView(title1);
        l1v.addView(t1);

        Button playbtn = new Button(this);
        playbtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        playbtn.setText("Play");
        playbtn.setId(i);
        playbtn.setEnabled(true);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mediaPlayer.isPlaying() && length==0) {
                    try {
                        mediaPlayer.setDataSource(link.get(i));
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                seekBar.setMax(mediaPlayer.getDuration());
                                mediaPlayer.start();
                                playbtn.setText("Pause");
                                mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                            }
                        });
                        mediaPlayer.prepare();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    length = mediaPlayer.getCurrentPosition();
                    System.out.println(length);
                    playbtn.setText("Resume");
                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                }
                else{
                    mediaPlayer.start();
                    playbtn.setText("Pause");
                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar,0);
                }
            }
        });


        l1h.addView(playbtn);
        l1h.addView(l1v);

        firstlayout.addView(l1h);
        this.layout.addView(firstlayout);
    }

//    public void playmusic(View view){
//        if(!mediaPlayer.isPlaying() && length==0){
//        try {
//            mediaPlayer.setDataSource("https://firebasestorage.googleapis.com/v0/b/musicapp-ad62e.appspot.com/o/Eminem%20-%20Lose%20Yourself%20%5BHD%5D.mp3?alt=media&token=50e3d705-0a76-44ab-a4fe-cee0beb42e68");
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mediaPlayer) {
//                    mediaPlayer.start();
//                }
//            });
//            mediaPlayer.prepare();
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
//    }
//        else if(mediaPlayer.isPlaying()){
//            mediaPlayer.pause();
//            length = mediaPlayer.getCurrentPosition();
//        }
//        else{
//            mediaPlayer.seekTo(length);
//            mediaPlayer.start();
//        }
//    }
//    public void stopmusic(View view){
//        mediaPlayer.seekTo(length);
//        mediaPlayer.start();
//    }




    protected void onStart () {
        super.onStart();
        String user = mAuth.getUid();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }
}