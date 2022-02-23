package com.unipi.mosdim.musicapp;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.FILL_HORIZONTAL;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    SeekBar seekBar;
    Button btnLogOut;
    FirebaseAuth mAuth;
    LinearLayout layout,l, firstlayout;
    ScrollView scrollView;
    MediaPlayer mediaPlayer = new MediaPlayer();
    TextView movingText,minText,maxText;
    ImageButton playbtn,nextbtn,previousbtn;
    ArrayList<String> songName = new ArrayList<>();
    ArrayList<String> artistName = new ArrayList<>();
    ArrayList<String> category = new ArrayList<>();
    ArrayList<String> link = new ArrayList<>();
    ArrayList<String> location = new ArrayList<>();
    String getLink="";
    int length=0,i;

    private Handler mSeekbarUpdateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = (SeekBar) findViewById(R.id.seekBar3);
        minText = findViewById(R.id.minTime);
        maxText = findViewById(R.id.maxTime);
        movingText = findViewById(R.id.movingText);
        movingText.setSelected(true);
        btnLogOut = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
        playbtn = findViewById(R.id.playbtn);
        nextbtn = findViewById(R.id.nextbtn);
        previousbtn = findViewById(R.id.previousbtn);
        btnLogOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
        DatabaseReference myRef;
        myRef = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("songs");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                i=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    songName.add((String) snapshot.child("name").getValue());   //τιτλος βιβλιων
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
            }
        });

//        DatabaseReference myRef2 = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("user_pref");
//        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    if (!((String)snapshot.child("genre").getValue()).equals("rock")){
//                        Toast.makeText(MainActivity.this, "Rock", Toast.LENGTH_SHORT).show();
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

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

    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(this, 50);
            setTime();
        }
    };

    public void setTime(){
        String time =(new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getCurrentPosition()));
        minText.setText(time);
        if(minText.getText().equals(maxText.getText())){
            int y=0;
            for (String element : link) {
                if (element == getLink) {
                    break;
                }
                y++;
            }
            if(!link.contains(getLink)) {
                playmusic(0);
            }
            if((link.size()-1)>y)
                playmusic(y+1);
            else if(link.size()-1<=y){
                getLink="";
                playmusic(0);
            }
        }
    }

    public void getAllData(int i){

        //hardcoded components
        layout = findViewById(R.id.layout_parent);
        scrollView = findViewById(R.id.scrollView_parent);

        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams lparams_inside = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        TextView t1 = new TextView(this);
        //t1.setLayoutParams(lparams);
        t1.setText(artistName.get(i));

        LinearLayout l1h = new LinearLayout(this);
        l1h.setWeightSum(1);    //controls the weights in l1v and playbtn
        l1h.setOrientation(LinearLayout.HORIZONTAL);
        l1h.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout l1v = new LinearLayout(this);
        l1v.setOrientation(LinearLayout.VERTICAL);
        lparams_inside.weight = 9;
        l1v.setLayoutParams(lparams_inside);

        TextView title1 = new TextView(this);
        //title1.setLayoutParams(lparams);
        title1.setText(songName.get(i));
        l1v.addView(title1);
        l1v.addView(t1);

        Button playbtn = new Button(this);
        lparams_inside.weight = 1;
        playbtn.setLayoutParams(lparams_inside);
        playbtn.setText("Play");
        playbtn.setId(i);
        playbtn.setEnabled(true);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playmusic(i);
            }
        });


        l1h.addView(l1v);
        l1h.addView(playbtn);

        //firstlayout.addView(l1h);
        this.layout.addView(l1h);
    }

    public void playmusic(int i){
        if(!getLink.equals(link.get(i))) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(link.get(i));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        playbtn.setImageResource(R.drawable.pauseimg);
                        getLink = link.get(i);
                        maxText.setText((new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getDuration())));
                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.start();
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1));
                        movingText.setText(songName.get(i)+","+artistName.get(i));
                    }
                });
                mediaPlayer.prepare();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else if(mediaPlayer.isPlaying()){
            playbtn.setImageResource(R.drawable.playimg);
            mediaPlayer.pause();
            length = mediaPlayer.getCurrentPosition();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }
        else{
            playbtn.setImageResource(R.drawable.pauseimg);
            mediaPlayer.start();
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar,0);
        }
    }
    public void playclick(View view){
        int y=0;
        for (String element : link) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if(!link.contains(getLink))
            y=0;
        playmusic(y);
    }

    public void previousbtn(View view) {
        int y = 0;
        for (String element : link) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if (!link.contains(getLink))
            y = 0;
        if (y > 0)
            playmusic(y - 1);
        else {
            getLink="";
            playmusic(0);
        }
    }
    public void nextbtn(View view){
        int y=0;
        for (String element : link) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if(!link.contains(getLink)) {
            playmusic(0);
        }
        if((link.size()-1)>y)
            playmusic(y+1);
        else if(link.size()-1<=y){
            getLink="";
            playmusic(0);
        }
    }

    public void searchGenre(View view){
        int i=0;
        layout.removeAllViews();
        if (!((EditText)findViewById(R.id.search_genreEditText)).getText().toString().isEmpty()){
            int found = 0;
            for (String song_genre: category) {
                if (song_genre.equals(((EditText)findViewById(R.id.search_genreEditText)).getText().toString())){
                    found++;
                    getAllData(i);
                }
                i++;
            }
            Toast.makeText(this, found + " song(s) found", Toast.LENGTH_SHORT).show();
        }
        else{
            for (int j = 0; j < songName.size(); j++) {
                getAllData(j);
            }
            Toast.makeText(this, songName.size() + " song(s) found", Toast.LENGTH_SHORT).show();
        }

    }

    protected void onStart () {
        super.onStart();
        String user = mAuth.getUid();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }
}