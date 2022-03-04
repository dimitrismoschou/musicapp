package com.unipi.mosdim.musicapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locman;
    static final int REQ_LOC_CODE = 23;
    private double latitude, longitude;

    SeekBar seekBar;
    Button buttonLogout;
    ImageButton search_button;
    Button homeButton, profileButton, settingsButton;
    FirebaseAuth mAuth;
    LinearLayout layout;
    ScrollView scrollView;
    MediaPlayer mediaPlayer = new MediaPlayer();
    TextView movingText,minText,maxText;
    Button playButton, nextButton, previousButton;
    EditText search;
    ArrayList<String> songNames = new ArrayList<>();
    ArrayList<String> artistNames = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();
    ArrayList<String> links = new ArrayList<>();
    ArrayList<String> locations = new ArrayList<>();
    ArrayList<Button> buttons = new ArrayList<>();
    ArrayList<Integer> arrayQueue = new ArrayList<>();
    String getLink="", country = "none";
    int length=0,i;

    private Handler mSeekbarUpdateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locman = (LocationManager) getSystemService(LOCATION_SERVICE);

        homeButton = findViewById(R.id.homebutton);
        profileButton = findViewById(R.id.profilebutton);
        settingsButton = findViewById(R.id.settingsbutton);
        search = findViewById(R.id.search_genreEditText);
        search_button = findViewById(R.id.imageButton);

        homeButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }

        });

        profileButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),UserProfile.class);
                startActivity(i);
            }

        });

        search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction()== KeyEvent.ACTION_DOWN) && (i == keyEvent.KEYCODE_ENTER)){
                    search_button.performClick();

                    //close keyboard
                    View current_view = MainActivity.this.getCurrentFocus();
                    if (current_view != null) {
                        InputMethodManager manager =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(current_view.getWindowToken(), 0);
                    }

                    return true;
                }
                return false;
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if((search.length() > 2 && i1<i2) || search.getText().toString().isEmpty())
                    search_button.performClick();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC_CODE);
        }
        else {
            locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        Location location = null;
        if (locman.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //maybe an onTaskCompleteListener would fit here instead of while
            while (locman.getLastKnownLocation(LocationManager.GPS_PROVIDER)==null){
                continue;
            }
            location = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            onLocationChanged(location);
            currentCountry(location);
        }
        else {
            enableLoc();
        }

        seekBar = (SeekBar) findViewById(R.id.seekBar3);
        minText = findViewById(R.id.minTime);
        maxText = findViewById(R.id.maxTime);
        movingText = findViewById(R.id.movingText);
        movingText.setSelected(true);
        buttonLogout = findViewById(R.id.buttonLogout);
        mAuth = FirebaseAuth.getInstance();
        playButton = findViewById(R.id.playButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);

        buttonLogout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            mediaPlayer.reset();
            finish();
        });
        DatabaseReference myRef;
        myRef = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("songs");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                i=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (country.equals("none")){
                        songNames.add((String) snapshot.child("name").getValue());
                        artistNames.add((String) snapshot.child("artist").getValue());
                        links.add((String) snapshot.child("link").getValue());
                        locations.add((String) snapshot.child("location").getValue());
                        categories.add((String) snapshot.child("category").getValue());
                        arrayQueue.add(i);
                        getAllData(i);
                        i++;
                    }
                    else if (snapshot.child("location").getValue().toString().toLowerCase().contains(country)){
                        songNames.add((String) snapshot.child("name").getValue());
                        artistNames.add((String) snapshot.child("artist").getValue());
                        links.add((String) snapshot.child("link").getValue());
                        locations.add((String) snapshot.child("location").getValue());
                        categories.add((String) snapshot.child("category").getValue());
                        arrayQueue.add(i);
                        getAllData(i);
                        i++;
                    }

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
            for (String element : links) {
                if (element == getLink) {
                    break;
                }
                y++;
            }
            if(!links.contains(getLink)) {
                playMusic(0);
            }
            if((links.size()-1)>y) {
                playMusic(y + 1);
            }
            else if(links.size()-1<=y){
                getLink="";
                playMusic(0);
            }
        }
    }

    public void getAllData(int i){
        //hardcoded components
        layout = findViewById(R.id.layout_parent);
        scrollView = findViewById(R.id.scrollView_parent);

        LinearLayout.LayoutParams lparams_inside = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        TextView t1 = new TextView(this);
        t1.setText(artistNames.get(i));

        LinearLayout l1h = new LinearLayout(this);
        l1h.setWeightSum(1);    //controls the weights in l1v and playbtn
        l1h.setOrientation(LinearLayout.HORIZONTAL);
        l1h.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout l1v = new LinearLayout(this);
        l1v.setOrientation(LinearLayout.VERTICAL);
        lparams_inside.weight = 9;
        l1v.setLayoutParams(lparams_inside);

        TextView title1 = new TextView(this);
        title1.setText(songNames.get(i));
        l1v.addView(title1);
        l1v.addView(t1);

        buttons.add(i,new Button(this));
        lparams_inside.weight = 1;
        buttons.get(i).setLayoutParams(lparams_inside);
        buttons.get(i).setBackgroundResource(R.drawable.ic_small_play);
        buttons.get(i).setEnabled(true);
        buttons.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusic(i);
            }
        });


        l1h.addView(l1v);
        l1h.addView(buttons.get(i));

        l1h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.get(i).performClick();
            }
        });

        if (i%2!=0){
            l1h.setBackgroundResource(R.color.lightgray);
        }

        this.layout.addView(l1h);
    }
    public void playMusic(int i){
        if(!getLink.equals(links.get(i))) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(links.get(i));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        for(Button btn1 : buttons)
                            btn1.setBackgroundResource(R.drawable.ic_small_play);
                        buttons.get(i).setBackgroundResource(R.drawable.ic_small_pause);
                        playButton.setBackgroundResource(R.drawable.pauseimg);
                        getLink = links.get(i);
                        maxText.setText((new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getDuration())));
                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.start();
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1));
                        movingText.setText(songNames.get(i)+","+ artistNames.get(i));
                    }
                });
                mediaPlayer.prepare();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else if(mediaPlayer.isPlaying()){
            buttons.get(i).setBackgroundResource(R.drawable.ic_small_play);
            playButton.setBackgroundResource(R.drawable.playimg);
            mediaPlayer.pause();
            length = mediaPlayer.getCurrentPosition();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }
        else{
            buttons.get(i).setBackgroundResource(R.drawable.ic_small_pause);
            playButton.setBackgroundResource(R.drawable.pauseimg);
            mediaPlayer.start();
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar,0);
        }
    }

    public void playClick(View view){
        int y=0;
        for (String element : links) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if(!links.contains(getLink))
            y=0;
        playMusic(y);
    }

    public void previousButton(View view) {
        int y = 0;

        if (arrayQueue.size() > 1) {
            for (String element : links) {
                if (!(links.get(arrayQueue.get(y)) == getLink)) {
                    y++;
                } else
                    break;
            }
            if (!links.contains(getLink))
                y = 0;
            if (y > 0)
                playMusic(arrayQueue.get(y - 1));
            else {
                getLink = "";
                playMusic(arrayQueue.get(0));
            }
        }
        else{
            getLink = "";
            playMusic(arrayQueue.get(0));
        }
    }
    public void nextButton(View view){
        int y=0;
        if((arrayQueue.size()>1)) {
            for (String element : links) {
                if (!(links.get(arrayQueue.get(y)) == getLink)) {
                    y++;
                } else
                    break;
            }

            if (!links.contains(getLink)) {
                playMusic(0);
            }
            if ((arrayQueue.size() - 1) > y)
                playMusic(arrayQueue.get(y + 1));
            else if ((arrayQueue.size() - 1) <= y) {
                getLink = "";
                playMusic(arrayQueue.get(0));
            }
        }
        else{
            getLink = "";
            playMusic(arrayQueue.get(0));
        }
    }
    int updatedQueueSize=0;
    public void searchGenre(View view){
        int i=0;
        layout.removeAllViews();
        arrayQueue.clear();
        if (!((EditText)findViewById(R.id.search_genreEditText)).getText().toString().isEmpty()){
            //int found = 0;
            for (String song_genre: categories) {
                if (song_genre.equals(((EditText)findViewById(R.id.search_genreEditText)).getText().toString())){
                    //found++;
                    arrayQueue.add(i);
                    getAllData(i);
                }
                i++;
            }
            updatedQueueSize = i;
            //Toast.makeText(this, found + " song(s) found", Toast.LENGTH_SHORT).show();
        }
        else{
            int j;
            for (j=0 ; j < songNames.size(); j++) {
                arrayQueue.add(j);
                getAllData(j);
            }
            updatedQueueSize = j;
            //Toast.makeText(this, songName.size() + " song(s) found", Toast.LENGTH_SHORT).show();
        }

    }


    protected void onStart () {
        super.onStart();
        String user = mAuth.getUid();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else {
            mAuth.getCurrentUser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOC_CODE && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    /**
     * Runs every time gps gets new location
     * @param location
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    /**
     * Runs every time user activates gps after prompt window
     * @param provider
     */
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Ignore
     * @param provider
     */
    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    /**
     * Writes the current country's name to country
     * @param location
     */
    private void currentCountry (Location location){
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            country = addresses.get(0).getCountryName().toLowerCase();
        } catch (IOException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Opens a popup window every time location is off, prompting user to enabled it
     */
    private void enableLoc() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {


            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        999);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

    }
    
}