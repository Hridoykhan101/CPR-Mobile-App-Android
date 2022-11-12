package com.bluetooth.pa2123.resus.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.R;
import com.bluetooth.pa2123.resus.Settings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class settings extends AppCompatActivity {

    private RadioGroup radioMeasurement;
    private RadioGroup radioCompTimer;
    private RadioButton measurement;
    private RadioButton timer;
    private Button logout;
    private Settings setting = Settings.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle(R.string.screen_title_settings);

        radioCompTimer = (RadioGroup)findViewById(R.id.radioCompTimer);
        radioMeasurement = (RadioGroup)findViewById(R.id.radioMeasurement);

        RadioButton timerSet;

        if(setting.getStartingTimer() == 30000) {
            timerSet = (RadioButton)findViewById(R.id.thirtySec);
            timerSet.setChecked(true);
        } else if(setting.getStartingTimer() == 60000) {
            timerSet = (RadioButton)findViewById(R.id.sixtySec);
            timerSet.setChecked(true);
        } else if(setting.getStartingTimer() == 120000) {
            timerSet = (RadioButton)findViewById(R.id.oneHundredTwentySec);
            timerSet.setChecked(true);
        }

        RadioButton measurementSet;

        if(setting.getMeasurement().equals("cm")) {
            measurementSet = (RadioButton) findViewById(R.id.centimeter);
            measurementSet.setChecked(true);
        } else if(setting.getMeasurement().equals("inches")) {
            measurementSet = (RadioButton) findViewById(R.id.inche);
            measurementSet.setChecked(true);
        }

        logout = (Button)findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(settings.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.settings);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())  {
                    case R.id.cprtest:
                        startActivity(new Intent(getApplicationContext(), cprTest.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        return true;
                    case R.id.history:
                        startActivity(new Intent(getApplicationContext(), cprListActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(settings.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setting.save(this);
    }

    public void checkMeasurement(View view) {
        int radioID = radioMeasurement.getCheckedRadioButtonId();
        measurement = findViewById(radioID);
        if(measurement.getText().toString().equals("Centimeters")) {
            setting.setMeasurement("cm");
        } else if(measurement.getText().toString().equals("Inches")) {
            setting.setMeasurement("inches");
        }
        Log.d(Constants.TAG,"Roth :: measurement Selected " + setting.getMeasurement());
    }

    public void checkCompTime(View view) {
        int radioID = radioCompTimer.getCheckedRadioButtonId();
        timer = findViewById(radioID);
        if(timer.getText().toString().equals("30 Seconds")) {
            setting.setStartingTimer(30000);
        } else if(timer.getText().toString().equals("60 Seconds")) {
            setting.setStartingTimer(60000);
        } else if(timer.getText().toString().equals("120 Seconds")) {
            setting.setStartingTimer(120000);
        }
        Log.d(Constants.TAG,"Roth :: timer Selected " + setting.getStartingTimer());
    }
}