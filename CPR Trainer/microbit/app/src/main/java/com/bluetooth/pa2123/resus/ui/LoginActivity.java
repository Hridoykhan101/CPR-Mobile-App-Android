package com.bluetooth.pa2123.resus.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.R;
import com.bluetooth.pa2123.resus.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static String loginName;
    public static String getUsername() {
        return loginName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //First, check if the stored preferences has a valid refresh token

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Welcome to Resus");
        Settings.getInstance().restore(this);

        //DatabaseHelper db = new DatabaseHelper(this);
        Activity activity = this;

        EditText usernameLog = (EditText)findViewById(R.id.username);
        EditText passwordLog = (EditText)findViewById(R.id.password);
        Button login = (Button)findViewById(R.id.login);
        Button signup = (Button)findViewById(R.id.register);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = usernameLog.getText().toString();
                String pw = passwordLog.getText().toString();
                if(user.equals("") || pw.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid Login Details", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {

                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject()
                                .put("email", user)
                                .put("password", pw);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RequestHelper.createTokenRequest(activity, jsonObject, "/person/login", MainActivity.class, null, null);

                    //JsonRequest request = createRequest(jsonObject, "/Person/Login");

                    /*
                    // Old Mysqlite login. Now connect to backend
                    usersRecords usertoCheck;
                    usertoCheck = new usersRecords(user,pw);
                    boolean success = db.checkUsernamePassword(usertoCheck);
                    if(success == true)  {
                        loginName = usernameLog.getText().toString();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid Login Details", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        Log.d(Constants.TAG, "Login failed");
                    }
                    */
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, registration.class);
                startActivity(intent);
            }
        });

    }

    /*
    public JsonObjectRequest createRequest(JSONObject jsonObject, String endpoint) {
        return new JsonObjectRequest(Request.Method.POST, getResources().getString(R.string.apiURL) + endpoint, jsonObject, response -> {
            System.out.println("This is the response: " + response);
            try {
                MainActivity.accessToken = response.getString("newAccessToken");
                MainActivity.refreshToken = response.getString("newRefreshToken");

                //Store the refresh token into shared preferences
                SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("refreshToken", MainActivity.refreshToken);
                editor.apply();

                //Now that the refresh token is stored in memory, move over to the main screen
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } catch (JSONException e) {
                System.out.println(e);
            }

        }, error -> {
            System.out.println("Response returned failure");
            System.out.println(error.toString());
        });
    }
     */

}
