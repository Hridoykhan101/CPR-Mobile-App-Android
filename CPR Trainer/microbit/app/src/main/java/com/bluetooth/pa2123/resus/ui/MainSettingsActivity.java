package com.bluetooth.pa2123.resus.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.R;
import com.bluetooth.pa2123.resus.Settings;

public class MainSettingsActivity extends AppCompatActivity {

    public static final int START_MAIN_SETTINGS = 1;
    public static final int START_MAIN_QR_SCAN = 2;
    private CheckBox cb_fud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        cb_fud = (CheckBox) this.findViewById(R.id.cb_scan_filter_unpaired);
        cb_fud.setChecked(Settings.getInstance().isFilter_unpaired_devices());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "MainSettingsActivity onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                this.setResult(RESULT_OK);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFilterChange(View view) {
        Log.d(Constants.TAG,"onFilterChange: "+cb_fud.isChecked());
        boolean checked = cb_fud.isChecked();
        Settings settings = Settings.getInstance();
        settings.setFilter_unpaired_devices(checked);
    }

}
