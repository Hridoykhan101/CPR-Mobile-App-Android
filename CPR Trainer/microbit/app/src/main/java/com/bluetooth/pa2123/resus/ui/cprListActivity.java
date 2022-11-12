package com.bluetooth.pa2123.resus.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.R;
import com.bluetooth.pa2123.resus.Settings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class cprListActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "username";
    private boolean ble_scanning = false;
    private Handler handler = new Handler();
    //private ListAdapter cpr_list_adapter;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    Intent intent = getIntent();
    ArrayAdapter cprArrayAdapter;
    ListView listView;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cprlist);
        getSupportActionBar().setTitle("CPR History List");

        Settings.getInstance().restore(this);

        databaseHelper = new DatabaseHelper(cprListActivity.this);
        listView = (ListView) this.findViewById(R.id.cpr_list);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.history);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull  MenuItem menuItem) {
                switch (menuItem.getItemId())  {
                    case R.id.cprtest:
                        startActivity(new Intent(getApplicationContext(), cprTest.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), settings.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.history:
                        return true;
                }
                return false;
            }
        });

        showCprOnListView(databaseHelper);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteItem(parent, position);
                return false;
            }
        });

    }

    public void deleteItem(AdapterView<?> parent, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cprListActivity.this);
        builder.setTitle("Delete Item");
        builder.setCancelable(true);
        builder.setMessage("Do you want to delete this result?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cprHistory clickedCPR = (cprHistory) parent.getItemAtPosition(position);
                databaseHelper.deleteOne(clickedCPR);
                showCprOnListView(databaseHelper);
            }
        });
        builder.setNegativeButton("No",null);
        builder.show();
    }

    private void showCprOnListView(DatabaseHelper databaseHelper) {
        final String username = LoginActivity.getUsername();
        listView.setAdapter(new ArrayAdapter<cprHistory>(cprListActivity.this, android.R.layout.simple_list_item_1,databaseHelper.getCPRHistory(username)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position,convertView,parent);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                return textView;
            }
        });

    }

    public void onBackPressed() {
        Intent intent = new Intent(cprListActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cprlist, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_cprlist_about) {
            Intent intent = new Intent(cprListActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.MAIN_ABOUT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
