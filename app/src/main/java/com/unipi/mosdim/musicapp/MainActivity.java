package com.unipi.mosdim.musicapp;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
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
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locman;
    static final int REQ_LOC_CODE = 23;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    SeekBar seekBar;
    ImageButton search_button;
    Button profileButton, settingsButton;
    FirebaseAuth mAuth;
    LinearLayout layout;
    ScrollView scrollView;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static String preference = "";
    public static String uid = "";
    TextView movingText, minText, maxText;
    Button playButton, nextButton, previousButton;
    EditText search;
    ArrayList<String> songNames = new ArrayList<>();
    ArrayList<String> artistNames = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();
    ArrayList<String> links = new ArrayList<>();
    ArrayList<String> locations = new ArrayList<>();
    ArrayList<Button> buttons = new ArrayList<>();
    ArrayList<Integer> arrayQueue = new ArrayList<>();
    String getLink = "", country = "none";
    int length = 0, i;
    SensorManager mySensorManager;
    Sensor myProximitySensor;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private boolean proximityFlag = true;

    private Handler mSeekbarUpdateHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locman = (LocationManager) getSystemService(LOCATION_SERVICE);
        profileButton = findViewById(R.id.profilebutton);
        settingsButton = findViewById(R.id.settingsbutton);
        search = findViewById(R.id.search_genreEditText);
        search_button = findViewById(R.id.imageButton);

        layout = findViewById(R.id.layout_parent);
        scrollView = findViewById(R.id.scrollView_parent);

        //Proximity sensor code
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = getWakeLock();
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (myProximitySensor == null) {
            Toast.makeText(this, "No Proximity Sensor!", Toast.LENGTH_SHORT).show();
            proximityFlag = false;
        } else {
            mySensorManager.registerListener(proximitySensorEventListener, myProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        profileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), UserProfile.class);
                startActivity(i);
            }
        });

        search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == keyEvent.KEYCODE_ENTER)) {
                    searchGenre();
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
                if ((search.length() > 2 && i1 < i2) || search.getText().toString().isEmpty())
                    searchGenre();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOC_CODE);
        }
        else {
            Location location = null;
            if ( !locman.isLocationEnabled()) {
                enableLoc();
            }
            else{
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }


        seekBar = (SeekBar) findViewById(R.id.seekBar3);
        minText = findViewById(R.id.minTime);
        maxText = findViewById(R.id.maxTime);
        movingText = findViewById(R.id.movingText);
        movingText.setSelected(true);

        mAuth = FirebaseAuth.getInstance();
        playButton = findViewById(R.id.playButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);

        DatabaseReference myRef;
        myRef = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("songs");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (preference == "") {
                        if (country.equals("none") || snapshot.child("location").getValue().toString().contains(country)) {
                            search.setEnabled(true);
                            search_button.setEnabled(true);
                            songNames.add((String) snapshot.child("name").getValue());
                            artistNames.add((String) snapshot.child("artist").getValue());
                            links.add((String) snapshot.child("link").getValue());
                            locations.add((String) snapshot.child("location").getValue());
                            categories.add((String) snapshot.child("category").getValue());
                            arrayQueue.add(i);
                            getAllData(i);
                            i++;
                        }
                    } else {
                        if ((country.equals("none") || snapshot.child("location").getValue().toString().contains(country))
                                && snapshot.child("category").getValue().toString().equals(preference.toLowerCase())) {
                            search.setEnabled(false);
                            search_button.setEnabled(false);
                            SettingsActivity.switcher = true;
                            songNames.add((String) snapshot.child("name").getValue());
                            artistNames.add((String) snapshot.child("artist").getValue());
                            links.add((String) snapshot.child("link").getValue());
                            locations.add((String) snapshot.child("location").getValue());
                            categories.add((String) snapshot.child("category").getValue());
                            arrayQueue.add(i);
                            getAllData(i);
                            i++;
                            search.setEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

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


    public void settings(View view) {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(i);
    }

    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(this, 50);
            setTime();
        }
    };


    public void setTime() {
        String time = (new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getCurrentPosition()));
        minText.setText(time);
        if (minText.getText().equals(maxText.getText())) {
            nextButton.performClick();
        }
    }

    public void getAllData(int i) {
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

        buttons.add(i, new Button(this));
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
        this.layout.addView(l1h);
    }

    public void playMusic(int i) {
        if (!getLink.equals(links.get(i))) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(links.get(i));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        for (Button btn1 : buttons)
                            btn1.setBackgroundResource(R.drawable.ic_small_play);
                        buttons.get(i).setBackgroundResource(R.drawable.ic_small_pause);
                        playButton.setBackgroundResource(R.drawable.pauseimg);
                        getLink = links.get(i);
                        maxText.setText((new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getDuration())));
                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.start();
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1));
                        movingText.setText(songNames.get(i) + "," + artistNames.get(i));
                    }
                });
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (mediaPlayer.isPlaying()) {
            buttons.get(i).setBackgroundResource(R.drawable.ic_small_play);
            playButton.setBackgroundResource(R.drawable.playimg);
            mediaPlayer.pause();
            length = mediaPlayer.getCurrentPosition();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        } else {
            buttons.get(i).setBackgroundResource(R.drawable.ic_small_pause);
            playButton.setBackgroundResource(R.drawable.pauseimg);
            mediaPlayer.start();
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
        }
    }

    public void playClick(View view) {
        int y = 0;
        for (String element : links) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if (!links.contains(getLink))
            y = 0;
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
        } else {
            getLink = "";
            playMusic(arrayQueue.get(0));
        }
    }

    public void nextButton(View view) {
        int y = 0;
        if ((arrayQueue.size() > 1)) {
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
        } else {
            getLink = "";
            playMusic(arrayQueue.get(0));
        }
    }

    int updatedQueueSize = 0;

    public void searchGenre() {
        int i = 0;
        layout.removeAllViews();
        arrayQueue.clear();
        if (!((EditText) findViewById(R.id.search_genreEditText)).getText().toString().isEmpty()) {
            for (String song_genre : categories) {
                if (song_genre.equals(((EditText) findViewById(R.id.search_genreEditText)).getText().toString().toLowerCase())) {
                    arrayQueue.add(i);
                    getAllData(i);
                    if (mediaPlayer.isPlaying()) {
                        for (int k = 0; k < 2; k++) {
                            int y = 0;
                            for (String element : links) {
                                if (element == getLink) {
                                    break;
                                }
                                y++;
                            }
                            if (!links.contains(getLink))
                                y = 0;
                            playMusic(y);
                        }
                    }
                }
                i++;
            }
            updatedQueueSize = i;
        } else {
            int j;
            for (j = 0; j < songNames.size(); j++) {
                arrayQueue.add(j);
                getAllData(j);
                if (mediaPlayer.isPlaying()) {
                    for (int k = 0; k < 2; k++) {
                        int y = 0;
                        for (String element : links) {
                            if (element == getLink) {
                                break;
                            }
                            y++;
                        }
                        if (!links.contains(getLink))
                            y = 0;
                        playMusic(y);
                    }
                }
            }
            updatedQueueSize = j;
        }

    }


    protected void onStart() {
        super.onStart();
        String user = mAuth.getUid();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            uid = user;
            mAuth.getCurrentUser();
        }
    }


    /**
     * Runs every time gps gets new location
     * @param location
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (country.equals("none"))
            currentCountry(location);
    }

    /**
     * Runs every time user activates gps after prompt window
     * @param provider
     */
    @Override
    public void onProviderEnabled(@NonNull String provider) {
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
     */
    private void currentCountry(Location location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            country = addresses.get(0).getCountryCode();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a popup window every time location is off, prompting user to enabled it
     */
    private void enableLoc() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void voiceRec(View view) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_LOC_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0) {
                    locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    recreate();
                }
                else {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    MainActivity.mediaPlayer.reset();
                    finish();
                }
                break;
            }
            case REQUEST_CODE_SPEECH_INPUT:{

                if(grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults.length>0){
                    Intent intent
                            = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                            Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
                    try {
                        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT); }
                    catch (Exception e) {
                        Toast
                                .makeText(MainActivity.this, " " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show(); }
                }
                else
                    Toast.makeText(MainActivity.this, R.string.reject_voice, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                search.setText("");
                search.setText(
                        Objects.requireNonNull(result).get(0).toLowerCase());
            }
        }
    }

    /**
     * Proximity's sensor listener, uses locks to enable or disable screen
     */
    SensorEventListener proximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
                if(event.values[0]<=0){
                    try {
                        wakeLock.acquire();
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "No proximity sensor!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    wakeLock.release();
                }
            }
        }
    };

    /**
     * Getter for wakeLock
     * @return wakeLock or null (if there is no sensor)
     */
    private PowerManager.WakeLock getWakeLock(){
        if(powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "AppName:tag");
            wakeLock.setReferenceCounted(false);
            return wakeLock;
        } else {
            return null;
        }
    }

    /**
     * Exclusively used for unregistering proximity Listener after app has paused
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (proximityFlag)
            mySensorManager.unregisterListener(proximitySensorEventListener);
    }

    /**
     * Exclusively used for registering proximity Listener app has resumed
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (proximityFlag)
            mySensorManager.registerListener(proximitySensorEventListener, myProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}