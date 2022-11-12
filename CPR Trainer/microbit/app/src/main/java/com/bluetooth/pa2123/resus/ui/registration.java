package com.bluetooth.pa2123.resus.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bluetooth.pa2123.resus.R;

import org.json.JSONException;
import org.json.JSONObject;

public class registration extends AppCompatActivity {

    private EditText username, password, confirmPassword, fName, lName, email;
    private Button signUp, gotoLogin;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setTitle("Registration");

        Activity activity = this;

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        fName = findViewById(R.id.firstname);
        lName = findViewById(R.id.lastname);
        email = findViewById(R.id.email);

        confirmPassword = (EditText)findViewById(R.id.passwordConfirmation);

        databaseHelper = new DatabaseHelper(this);

        signUp = (Button)findViewById(R.id.register);
        gotoLogin = (Button)findViewById(R.id.existingUser);
        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(registration.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pw = password.getText().toString();
                String cpw = confirmPassword.getText().toString();
                String eml = email.getText().toString();
                String fN = fName.getText().toString();
                String lN = lName.getText().toString();


                if(user.equals("") || pw.equals("") || cpw.equals("") || eml.equals("") || fN.equals("") || lN.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Fill all the Fields", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    if(pw.equals(cpw)) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject()
                                    .put("username", user)
                                    .put("fName", fN)
                                    .put("lName", lN)
                                    .put("email", eml)
                                    .put("password", pw);

                            RequestHelper.createTokenRequest(activity, jsonObject, "/person/register", MainActivity.class, null, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        /*
                        // Old logic, which uses the sqlite db
                        Boolean checkUser = databaseHelper.checkUser(user);
                        if(checkUser == false) {
                            usersRecords usertoAdd;
                            try {
                                usertoAdd = new usersRecords(user,pw);
                                Toast toast = Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            } catch (Exception e) {
                                usertoAdd = new usersRecords("","");
                                Toast toast = Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            databaseHelper.addUser(usertoAdd);
                            Intent intent = new Intent(registration.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        */
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
            }
        });
    }
}