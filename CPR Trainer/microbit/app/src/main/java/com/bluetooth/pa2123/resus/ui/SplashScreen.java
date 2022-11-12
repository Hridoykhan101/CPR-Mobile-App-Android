package com.bluetooth.pa2123.resus.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bluetooth.pa2123.resus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Repeatable;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //region Icon animate
        //Initiate animation for the icon
        ImageView iconLogo = findViewById(R.id.iconLogo);

        ScaleAnimation iconAnim = new ScaleAnimation(1f,1.2f,1f,1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        iconAnim.setDuration(1000);
        iconAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        iconAnim.setRepeatMode(Animation.REVERSE);
        iconAnim.setRepeatCount(Animation.INFINITE);
        iconLogo.setAnimation(iconAnim);
        //endregion

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String refreshToken = sharedPref.getString("refreshToken", "");

        //*
        if (!refreshToken.equals("")) {
            //Perhaps we have a valid refresh token. Let's see if we get a valid response


            Toast toast = Toast.makeText(getApplicationContext(), "Refresh token found: " + refreshToken, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject()
                        .put("refreshToken", refreshToken);

                RequestHelper.createTokenRequest(getApplicationContext(), jsonObject, "/refresh", MainActivity.class, LoginActivity.class, null);
            } catch (JSONException e) {
                e.printStackTrace();

                System.out.println("Error creating token request");

                //Error with performing the request. Send to login screen
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }


        } else {
            System.out.println("Refresh token was not found");

            //Refresh token doesn't exist. Get the user to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

         //*/
    }
}