package com.bluetooth.pa2123.resus.ui;

import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;

import com.android.volley.Request;
import com.bluetooth.pa2123.resus.CallableParam;
import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.MicroBit;
import com.bluetooth.pa2123.resus.R;
import com.bluetooth.pa2123.resus.Settings;
import com.bluetooth.pa2123.resus.Utility;
import com.bluetooth.pa2123.resus.bluetooth.BleAdapterService;
import com.bluetooth.pa2123.resus.bluetooth.ConnectionStatusListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pl.pawelkleczkowski.customgauge.CustomGauge;

import static android.view.View.INVISIBLE;

public class cprTest extends AppCompatActivity implements ConnectionStatusListener {

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ID = "id";
    private CustomGauge gauge3;
    private BleAdapterService bluetooth_le_adapter;

    private boolean exiting=false;
    private boolean indications_on=false;
    private int potentiometer_reading=0;
    private float compression_depth=0;
    private int totalCount =0;
    private float compression_rate = 0;
    private int goodComp =0;
    private boolean count_start = false;
    private boolean goodCount = false;
    private DecimalFormat df2 = new DecimalFormat("#.##");
    //The time between receiving bluetooth data from the microbit. Used for obtaining the waveform
    private long timeSinceLastPoll = System.currentTimeMillis();
    private boolean firstPoll = true;

    private TextView countdownText;
    private Button countdownButton;

    private CountDownTimer countDownTimer;
    private long initialTime = Settings.getInstance().getStartingTimer();
    private long timeLeftInMilliseconds = initialTime;
    private boolean timeRunning;
    private int timeElapse;
    private List<float[]> waveform = new ArrayList<>();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            connectToDevice();

            if (bluetooth_le_adapter.setIndicationsState(Utility.normaliseUUID(BleAdapterService.UARTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.UART_TX_CHARACTERISTIC_UUID), true)) {
                Log.d(Constants.TAG,"bluetooth service CONNECTED ON");
            } else {
                Log.d(Constants.TAG,"Failed to set bluetooth service indications ON");
            }

            Log.d(Constants.TAG, "bluetooth service indications ON");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_cpr_test);
        getSupportActionBar().setTitle(R.string.screen_title_CPR_test);
        gauge3 = (CustomGauge) findViewById(R.id.gauge3);
        countdownText = (TextView)findViewById(R.id.timer);
        countdownButton = (Button)findViewById(R.id.button);

        Log.d(Constants.TAG, "Roth :: CprTest OnCreate");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.cprtest);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull  MenuItem menuItem) {
                switch (menuItem.getItemId())  {
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), settings.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.cprtest:
                        return true;
                    case R.id.history:
                        startActivity(new Intent(getApplicationContext(), cprListActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setVisibility(INVISIBLE);

        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setVisibility(View.VISIBLE);
                startStop();
            }
        });
        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setMicrobit_name(intent.getStringExtra(EXTRA_NAME));
        MicroBit.getInstance().setMicrobit_address(intent.getStringExtra(EXTRA_ID));
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(Constants.TAG, "JIMBO:Connected to Bluetooth Services");

    }

    private void connectToDevice() {
        if (bluetooth_le_adapter.connect(MicroBit.getInstance().getMicrobit_address())) {
        } else {
            //showMsg(Utility.htmlColorRed("onConnect: failed to connect"));
        }
    }

    public void startStop() {
        if(timeRunning) {
            stopTimer();
        } else {
            startTimer();
            countdownButton.setVisibility(INVISIBLE);
        }
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                if(timeElapse == 0) {
                    countdownButton.setVisibility(INVISIBLE);
                    savingAlert();
                }
                timeRunning = false;
                countdownButton.setText("Start");
            }
        }.start();

        countdownButton.setText("STOP");
        timeRunning = true;
    }

    public void stopTimer() {
        countDownTimer.cancel();
        countdownButton.setText("START");
        countdownButton.setVisibility(INVISIBLE);
        timeRunning = false;
    }

    public int updateTimer() {
        int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
        int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;
        int totalTime = minutes * 60 + seconds;

        String timeLeftText = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);

        countdownText.setText(timeLeftText);
        return totalTime;
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.TAG, "onDestroy");
        super.onDestroy();
        if (indications_on) {
            exiting = true;
            bluetooth_le_adapter.setIndicationsState(Utility.normaliseUUID(BleAdapterService.UARTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.UART_TX_CHARACTERISTIC_UUID), false);
        }
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }


    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        if (MicroBit.getInstance().isMicrobit_connected() && indications_on) {
            exiting = true;
            bluetooth_le_adapter.setIndicationsState(Utility.normaliseUUID(BleAdapterService.UARTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.UART_TX_CHARACTERISTIC_UUID), false);
        }
        exiting=true;
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            try {
                bluetooth_le_adapter.disconnect();
                // may already have unbound. No API to check state so....
                unbindService(mServiceConnection);
            } catch (Exception e) {
            }
        }
        finish();
        exiting=true;
        Intent intent = new Intent(cprTest.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cpr_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_reset) {
            onResetTimer();
            return true;
        }
        if (id == R.id.menu_uart_avm_help) {
            Intent intent = new Intent(cprTest.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.CPR_TEST_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Service message handler�//////////////////
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            timeElapse = updateTimer();
            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            String descriptor_uuid = "";
            byte[] b = null;
            TextView value_text = null;

            switch (msg.what) {
                case BleAdapterService.GATT_CONNECTED:
                    Log.d(Constants.TAG,"Roth:: Connected");
                    bluetooth_le_adapter.discoverServices();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    Log.d(Constants.TAG,"Roth:: Disconnected");
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    try {
                        Log.d(Constants.TAG, "Roth :: XXXX Services discovered");
                        List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices();
                        for (BluetoothGattService svc : slist) {
                            Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                            MicroBit.getInstance().addService(svc);
                            Log.d(Constants.TAG, "Roth ::  running");
                        }
                        MicroBit.getInstance().setMicrobit_services_discovered(true);
                        Log.d(Constants.TAG, "Roth ::  end");
                        Intent intent = new Intent(cprTest.this, cprTest.class);
                        startActivity(intent);
                    } catch (NullPointerException e) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(cprTest.this)
                                .setTitle("Error")
                                .setMessage("Please ensure the CPR dummy is turned on, then try again")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Do nothing
                                    }
                                });

                        alertDialog.show();
                        Log.e("ERROR", "Was unable to get GATT SERVICES");
                        finish();
                        finish();
                    }
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Roth :: Written");
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Roth :: Descriptor");
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    if (!exiting) {
                        indications_on=true;
                    } else {
                        indications_on=false;
                        finish();
                    }
                    break;
                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    Log.d(Constants.TAG, "Notification");

                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    potentiometer_reading = Integer.parseInt(Utility.byteArrayAsHexString(b));

                    if (timeRunning) {
                        long pollTimeDelta = (System.currentTimeMillis() - timeSinceLastPoll);
                        if (firstPoll) {
                            pollTimeDelta = 0;
                            firstPoll = false;
                            waveform.clear();
                        }
                        Log.d(Constants.TAG, "Time since last poll: " + pollTimeDelta + "ms");
                        timeSinceLastPoll = System.currentTimeMillis();

                        //Add to list
                        waveform.add(new float[]{pollTimeDelta, potentiometer_reading});
                    }

                    //Log.d(Constants.TAG, "JIMBO:Value=" + Utility.byteArrayAsHexString(b));

                    if(potentiometer_reading >= 450 && timeRunning == true) {
                        Log.d(Constants.TAG, "countStart=" + count_start);
                        count_start = true;
                        if(potentiometer_reading >= 800) {
                            goodCount = true;
                        }
                    }

                    if(potentiometer_reading <= 400 && count_start == true) {
                        totalCount = totalCount + 1;
                        if(goodCount == true) {
                            goodComp = goodComp + 1;
                            goodCount = false;
                        }
                        count_start = false;
                        Log.d(Constants.TAG, "countStart=" + count_start);
                    }
                    float timeToRate = initialTime / 1000;
                    compression_rate = (float) (totalCount / (float)((int)timeToRate - timeElapse))*60;
                    Log.d(Constants.TAG,"Roth :: timer Elapse "+ timeElapse);
                    compression_depth = (float) (((potentiometer_reading - 350.00) / 96.14));
                    showCompression();
                    break;
                case BleAdapterService.MESSAGE:
                    Log.d(Constants.TAG, "Message");
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
            }
        }
    };


    private void showCompression() {
        float cd = 0;
        if(compression_depth < 0) {
            cd = 0;
        } else {
            cd = compression_depth;
        }
        int pr = 0;
        if(potentiometer_reading < 350) {
            pr = 350;
        } else {
            pr = potentiometer_reading;
        }
        final int cc = totalCount;
        final int gc = goodComp;
        final float cr = compression_rate;

        int finalPr = pr;
        float finalCd;
        if(Settings.getInstance().getMeasurement().equals("cm")) {
            finalCd = cd;
        } else {
            finalCd = (float) (cd * 0.393701);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(timeRunning == true) {
                    ((TextView) cprTest.this.findViewById(R.id.compression_rate)).setText((df2.format(cr)));
                } else {
                    ((TextView) cprTest.this.findViewById(R.id.compression_rate)).setText("0.00");
                }
                if(Settings.getInstance().getMeasurement().equals("cm")) {
                    ((TextView) cprTest.this.findViewById(R.id.depth)).setText((df2.format(finalCd)));
                    ((TextView) cprTest.this.findViewById(R.id.textView9)).setText("Depth (cm)");
                } else {
                    ((TextView) cprTest.this.findViewById(R.id.depth)).setText((df2.format(finalCd)));
                    ((TextView) cprTest.this.findViewById(R.id.textView9)).setText("Depth (inches)");
                }
                ((TextView) cprTest.this.findViewById(R.id.compression_count)).setText(Integer.toString(cc));
                ((TextView) cprTest.this.findViewById(R.id.good_compression)).setText(Integer.toString(gc));
                ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setTextSize(34);
                ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setTypeface(Typeface.DEFAULT_BOLD);
                if(cr >= 100 && cr <= 120) {
                    ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setTextColor(Color.GREEN);
                    ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setText("GOOD");
                } else if(cr > 120) {
                    ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setTextColor(Color.RED);
                    ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setText("SLOW DOWN");
                } else if (cr < 100) {
                    ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setTextColor(Color.RED);
                    ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setText("SPEED UP");
                }
                gauge3.setValue(finalPr);
            }
        });
    }

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (connected) {
            Log.d(Constants.TAG,"Connected");
        } else {
            Log.d(Constants.TAG,"Disconnected");
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {
    }



    public void onResetTimer() {
        if(timeRunning == false && timeLeftInMilliseconds == 60000) {
            Toast toast = Toast.makeText(getApplicationContext(), "Test Already reset", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Log.d(Constants.TAG, "Reset");
            count_start = false;
            countDownTimer.cancel();
            countdownButton.setText("START");
            timeRunning = false;
            countdownButton.setVisibility(View.VISIBLE);
            ((TextView) cprTest.this.findViewById(R.id.compressionQuality)).setVisibility(INVISIBLE);
            timeLeftInMilliseconds = initialTime;
            goodComp = 0;
            totalCount = 0;
            showCompression();
        }
    }

    public void savingAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Data");
        builder.setCancelable(true);
        builder.setMessage("Do you want to save the result?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Calendar cal = Calendar.getInstance();
                final String currentDate = DateFormat.getDateTimeInstance().format(cal.getTime());
                final String username = LoginActivity.getUsername();
                final float compRate = compression_rate;
                final int compCount = totalCount;
                final int good_comp = goodComp;
                final float compFrac = (float) (((float)good_comp / (float)compCount) * 100.00);
                final int timer = (int) (initialTime / 1000);
                final String CF = df2.format(compFrac);



                try {
                    //Put the waveform into a 2D array
                    JSONArray jsonWaveform = new JSONArray();
                    for(float[] entry : waveform) {
                        JSONArray arr = new JSONArray();
                        for(float val : entry) {
                            arr.put(val);
                        }
                        jsonWaveform.put(arr);
                    }

                    JSONObject jsonNested = new JSONObject()
                            .put("waveform", jsonWaveform)
                            .put("compressionCount", totalCount)
                            .put("compressionGood", goodComp)
                            .put("sessionTime", initialTime);

                    JSONObject jsonObject = new JSONObject()
                            .put("version", "1.0.0")
                            .put("data", jsonNested)
                            .put("dummyId", MainActivity.dummy.id);

                    //Using this data, we will send it to the database
                    RequestHelper.createRequest(getApplicationContext(), Request.Method.POST, jsonObject, "/result", new CallableParam<JSONObject>() {
                        @Override
                        public Void call() throws Exception {
                            if (responseCode == 200) {
                                Toast.makeText(getApplicationContext(), "Successfully uploaded results", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to upload the results", Toast.LENGTH_LONG).show();
                                if (param != null) {
                                    Log.e(Constants.TAG, param.toString());
                                }
                            }
                            return null;
                        }
                    });
                } catch (JSONException e) {
                    Log.e(Constants.TAG, e.getMessage());
                }


                /*
                cprHistory CPR;
                try {
                    CPR = new cprHistory(-1,username,compRate,compCount,good_comp,CF,currentDate,timer);
                    Toast.makeText(cprTest.this, "CPR ADDED", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    CPR = new cprHistory(-1,"", 0,0,0,"","",0);
                    Toast.makeText(cprTest.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
                DatabaseHelper databaseHelper = new DatabaseHelper(cprTest.this);
                databaseHelper.addOne(CPR);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(cprTest.this);
                builder1.setCancelable(true);
                builder1.setMessage("Do you want to see results?");
                builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(Constants.TAG, "RESUS ::: Intent Starting.");
                        Intent intent = new Intent(getBaseContext(), cprListActivity.class);
                        startActivity(intent);
                    }
                });
                builder1.setNegativeButton("No thanks",null);
                builder1.show();

                 */
            }
        });
        builder.setNegativeButton("No",null);
        builder.show();
    }

}
