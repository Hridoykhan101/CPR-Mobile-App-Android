package com.bluetooth.pa2123.resus.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bluetooth.pa2123.resus.CallableParam;
import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.JsonApiRequest;
import com.bluetooth.pa2123.resus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import androidx.appcompat.app.AppCompatActivity;

public class RequestHelper {

    static public void createRequest(Context context, int method, JSONObject jsonObject, String endpoint, CallableParam<JSONObject> callback) {
        if (MainActivity.refreshToken.equals("")) {
            //We have already tried to refresh our token and have failed. Stop here
            return;
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        //Create a JSON body request
        JsonApiRequest jsonObjectRequest = new JsonApiRequest(method, context.getResources().getString(R.string.apiURL) + endpoint, jsonObject, response -> {
            System.out.println("This is the response: " + response);
            try {
                //Perform callback on successful response
                //Perhaps the response could be either a json array or object
                JSONObject responseJson = null;
                try {
                    JSONArray jsonArray1 = new JSONArray(response);
                    responseJson = jsonArray1.getJSONObject(0);
                } catch (JSONException e) {
                    try {
                        responseJson = new JSONObject(response);
                    } catch (JSONException e1) {
                        Log.e("WARNING", "Ultimately failed to retrieve a body from the server");
                        e.printStackTrace();
                    }
                }

                callback.setParam(responseJson);
                callback.setResponseCode(200); //For a quick and easy solution, we will assume all positive responses are 200
                callback.call();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }, error -> {
            System.out.println("Response returned failure");
            //System.out.println(error.toString());

            //The server sent back a 401 response. This means that the access token has expired. Refresh the token and perform the request again
            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                System.out.println("Safe to ignore prior message. Just refreshing token");
                //Perhaps the access token has expired? Obtain new access token and retry
                try {
                    //Build the body that contains the refresh token
                    JSONObject jsonBody = new JSONObject().put("refreshToken", MainActivity.refreshToken);

                    //Perform refresh token request. Once that's completed, perform a callback which performs this request again
                    createTokenRequest(context, jsonBody, "/refresh", null, null, new CallableParam() {
                        @Override
                        public Void call() throws Exception {
                            //Then perform this request again
                            createRequest(context, method, jsonObject, endpoint, callback);

                            return null;
                        }
                    });
                } catch(JSONException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                try {
                    // Supply parameters to the callback.
                    callback.setParam(new JSONObject(new String(error.networkResponse.data)));
                    callback.setResponseCode(error.networkResponse.statusCode);
                } catch (JSONException e) {
                    Log.e("ERROR", e.getMessage());
                } catch (NullPointerException e) {
                    Log.e("WARNING", "Network response was null");
                }
                try {
                    // Run callback
                    callback.call();
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
            }
        }) {
            // Make our request contain the "authorization" header, which includes the access token
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> customHeaders = new HashMap<>();
                customHeaders.put("authorization", "Bearer " + MainActivity.accessToken);

                return customHeaders;
            }
        };

        // Add jsonRequest into queue
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest); //run the request
    }

    static public void createTokenRequest(Context context, JSONObject jsonObject, String endpoint, Class onSuccess, Class onFailure, CallableParam callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, context.getResources().getString(R.string.apiURL) + endpoint, jsonObject, response -> {
            System.out.println("This is the response: " + response);
            try {
                //Grab tokens from JSON request body
                MainActivity.accessToken = response.getString("newAccessToken");
                MainActivity.refreshToken = response.getString("newRefreshToken");

                //Store the refresh token into shared preferences (secure android storage, unless rooted)
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("refreshToken", MainActivity.refreshToken);
                editor.apply();

                //Now that the refresh token is stored in memory, move over to the main screen
                if (onSuccess != null) {
                    Intent intent = new Intent(context, onSuccess);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);   //We never want the user to navigate back to the previous screen
                    context.startActivity(intent);
                }
            } catch (JSONException e) {
                System.out.println(e.getMessage());

                // Invalidate our stored refresh token. Prevent the endless request loop
                MainActivity.refreshToken = "";

                //Store the refresh token into shared preferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("refreshToken", MainActivity.refreshToken);
                editor.apply();

                if (onFailure != null) {
                    Intent intent = new Intent(context, onFailure);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            }

            if (callback != null) {
                try {
                    //Apply callback parameters. Run callback
                    callback.setParam(response);
                    callback.setResponseCode(200);
                    callback.call();
                } catch (Exception e) {
                    Log.e(Constants.TAG, e.getMessage());
                }
            }

        }, error -> {
            System.out.println("Response returned failure");
            System.out.println(error.toString());

            if (error.networkResponse != null) {
                try {
                    System.out.println((new JSONObject(new String(error.networkResponse.data))).toString());
                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (onFailure != null) {
                Intent intent = new Intent(context, onFailure);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.getApplicationContext().startActivity(intent);
            }

            if (callback != null) {
                try {
                    assert error.networkResponse != null;
                    callback.setResponseCode(error.networkResponse.statusCode);
                    callback.setParam(new JSONObject(new String(error.networkResponse.data)));
                } catch (JSONException e) {
                    Log.e("ERROR", e.getMessage());
                } catch (NullPointerException e) {
                    Log.e("WARNING", "Network response was null");
                }
            }
        });

        // Add jsonRequest into queue (perform the request)
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest);
    }
}
