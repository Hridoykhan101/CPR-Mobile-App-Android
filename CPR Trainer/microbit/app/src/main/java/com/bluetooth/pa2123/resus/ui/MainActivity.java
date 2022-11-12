package com.bluetooth.pa2123.resus.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.bluetooth.pa2123.resus.CallableParam;
import com.bluetooth.pa2123.resus.Constants;
import com.bluetooth.pa2123.resus.Dummy;
import com.bluetooth.pa2123.resus.MicroBit;
import com.bluetooth.pa2123.resus.Misc;
import com.bluetooth.pa2123.resus.R;
import com.bluetooth.pa2123.resus.Settings;
import com.bluetooth.pa2123.resus.Utility;
import com.bluetooth.pa2123.resus.bluetooth.BleScanner;
import com.bluetooth.pa2123.resus.bluetooth.BleScannerFactory;
import com.bluetooth.pa2123.resus.bluetooth.ScanResultsConsumer;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ScanResultsConsumer {

    private boolean ble_scanning = false;
    private Handler handler = new Handler();
    private ListAdapter ble_device_list_adapter;
    private BleScanner ble_scanner;
    private BroadcastReceiver receiver;
    private static final long SCAN_TIMEOUT = 30000;
    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_CAMERA = 2;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean permissions_granted=false;
    private static final String DEVICE_NAME_START = "BBC micro";
    private int device_count=0;
    private Toast toast;

    public static String accessToken = "";
    public static String refreshToken = "";

    public static Dummy dummy = new Dummy();

    static class ViewHolder {
        public TextView text;
        public TextView bdaddr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setButtonText();
        getSupportActionBar().setTitle(R.string.screen_title_main);
        showMsg(Utility.htmlColorGreen("Ready"));

        Settings.getInstance().restore(this);

        ble_device_list_adapter = new ListAdapter();

        ListView listView = (ListView) this.findViewById(R.id.deviceList);
        listView.setAdapter(ble_device_list_adapter);

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        ble_scanner = BleScannerFactory.getBleScanner(this.getApplicationContext());
        ble_scanner.setDevice_name_start(DEVICE_NAME_START);
        ble_scanner.setSelect_bonded_devices_only(true);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                BluetoothDevice device = ble_device_list_adapter.getDevice(position);
                StartCprTest(device);

            }
        });

        //Grey out QR Scan button if not available
        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Button btnScanQR = this.findViewById(R.id.btnScanQR);
            btnScanQR.setEnabled(false);
        }
    }

    private void StartCprTest(BluetoothDevice device) {
        if (ble_scanning) {
            setScanState(false);
            ble_scanner.stopScanning();
        }

        //*
        if (device.getBondState() == BluetoothDevice.BOND_NONE && Settings.getInstance().isFilter_unpaired_devices()) {
            device.createBond();

            showMsg(Utility.htmlColorRed("Selected micro:bit must be paired - pairing now"));
            return;
        }
        // */
        try {
            MainActivity.this.unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            // ignore!
        }
        if (toast != null) {
            toast.cancel();
        }
        MicroBit microbit = MicroBit.getInstance();
        microbit.setBluetooth_device(device);
        Intent intent = new Intent(MainActivity.this, cprTest.class);
        intent.putExtra(cprTest.EXTRA_NAME, device.getName());
        intent.putExtra(cprTest.EXTRA_ID, device.getAddress());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.d(Constants.TAG,e.getClass().getCanonicalName()+":"+e.getMessage());
        }
        Settings.getInstance().save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_main_settings) {
            Intent intent = new Intent(MainActivity.this, MainSettingsActivity.class);
            startActivityForResult(intent, MainSettingsActivity.START_MAIN_SETTINGS);
            return true;
        }
        if (id == R.id.menu_main_help) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.MAIN_HELP);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_main_about) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.MAIN_ABOUT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == MainSettingsActivity.START_MAIN_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                setButtonText();
                showMsg(Utility.htmlColorGreen("Ready"));
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
            }
        }
        if (requestCode == MainSettingsActivity.START_MAIN_QR_SCAN) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                //This is the QR code contents. Try pairing with Mac address first
                //Then check if it's in the database
                //Otherwise, request to create a new one

                //byte[] bytes = Misc.hexStringToByteArray(contents);


                System.out.println("Content: " + contents);

                if (contents.length() == 20) {
                    //A valid QR Code
                    byte[] bytes = contents.getBytes();

                    //First 6 bytes is the MAC Address of the dummy
                    byte[] macAddress = new byte[12];
                    System.arraycopy(bytes, 0, macAddress, 0, macAddress.length);
                    byte[] code = new byte[8];
                    System.arraycopy(bytes, 12, code, 0, code.length);

                    System.out.println("macAddress: " + new String(macAddress));

                    BigInteger integer = (new BigInteger(new String(macAddress), 16));
                    System.out.println("Big int: " + integer.toString());
                    MainActivity.dummy.macAddress = integer;
                    MainActivity.dummy.macAddressHex = new String(macAddress);
                    MainActivity.dummy.qrCode = Misc.hexToAscii(new String(code));

                    System.out.println("Code from QR Code: " + MainActivity.dummy.qrCode);
                    System.out.println("Mac address in decimal: " + MainActivity.dummy.macAddress);

                    //Let's see if the Microbit already exists in the database
                    RequestHelper.createRequest(this, Request.Method.GET, null, "/dummy/qr/" + MainActivity.dummy.qrCode, new CallableParam<JSONObject>() {
                        @Override
                        public Void call() throws Exception {
                            obtainDummy(param, MainActivity.dummy.qrCode, MainActivity.dummy.macAddress);

                            return null;
                        }
                    });
                } else {
                    //This one is not valid
                    simpleToast("Invalid QR Code", Toast.LENGTH_LONG);
                }

            }
        }
    }

    public void obtainDummy(JSONObject jsonObject) throws Exception {
        try {
            Log.i(Constants.TAG, "About to retrieve dummy info from jsonObject");
            int dummyId = jsonObject.getInt("id");
            int organisationId = -1;
            try {
                organisationId = Integer.parseInt(jsonObject.getString("organisationId"));
            } catch (JSONException e) {
                //Do nothing
            } catch (NumberFormatException e) {
                //Do nothing
            }
            Log.i(Constants.TAG, "Attempted to obtain organisationId, now proceeding with obtaining other data");
            String qrCode = jsonObject.getString("qrCode");
            BigInteger foundMacAddress = new BigInteger( jsonObject.getString("macAddress"));
            String name = jsonObject.getString("name");
            int personId = jsonObject.getInt("personId");    //Owner of the dummy


            Log.i(Constants.TAG, "About to compare mac addresses");

            Log.i(Constants.TAG, MainActivity.dummy.macAddress.toString());
            Log.i(Constants.TAG, foundMacAddress.toString());
            if (MainActivity.dummy.macAddress.equals(foundMacAddress)) {

                MainActivity.dummy.qrCode = qrCode;
                MainActivity.dummy.id = dummyId;
                MainActivity.dummy.name = name;
                MainActivity.dummy.organisationId = organisationId;
                MainActivity.dummy.ownerId = personId;

                Log.i(Constants.TAG, "About to get bluetooth device");
                BluetoothDevice bluetoothDevice = null;
                try {
                    String actualMac = Misc.hexToMac(MainActivity.dummy.macAddressHex);

                    if (BluetoothAdapter.checkBluetoothAddress(actualMac)) {
                        Log.i(Constants.TAG, "Mac address: " + actualMac + " is valid");
                    } else {
                        Log.i(Constants.TAG, "Mac address: " + actualMac + " is not valid");
                    }

                    if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {

                        bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(actualMac);

                        StartCprTest(bluetoothDevice);

                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Error")
                                .setMessage("Please make sure bluetooth is enabled, then try again")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Do nothing
                                    }
                                });

                        alertDialog.show();
                    }
                }  catch (IllegalArgumentException e) {
                    Log.e(Constants.TAG, "Invalid mac address");
                }
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Critical error!")
                        .setMessage("A mac address mismatch with the QR code has been detected. Please contact support!")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        });

                alertDialog.show();
            }

        } catch (JSONException e) {
            Log.e(Constants.TAG, e.getMessage());
            throw new Exception("Dummy was not found in json object");
        }
    }

    public boolean obtainDummy(JSONObject jsonObject, String qrCode, BigInteger macAddressDec) {
        try {
            obtainDummy(jsonObject);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Failed to obtain a dummy from the backend. Attempt to request the user to create a new dummy");
            Log.e(Constants.TAG, e.getMessage());
            //Use an Alert Dialogue to let the user enter a new dummy name
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("New Dummy");
            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setMessage("Please enter a new name for the dummy");

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Created a dummy under this mac address
                    MainActivity.dummy.name = input.getText().toString();
                    MainActivity.dummy.qrCode = new String(qrCode);
                    MainActivity.dummy.macAddress = macAddressDec;

                    Gson gson = new Gson();

                    try {
                        //Dummy obj to JSON format
                        JSONObject dummyObj = new JSONObject(gson.toJson(MainActivity.dummy));

                        // Send request to create the new dummy
                        RequestHelper.createRequest(getApplicationContext(), Request.Method.POST, dummyObj, "/dummy", new CallableParam<JSONObject>() {
                            @Override
                            public Void call() throws Exception {
                                obtainDummy(param);

                                return null;
                            }
                        });
                    } catch (JSONException e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);

            builder.show();
        }
        return true;
    }

    public void onScan(View view) {
        if (!ble_scanner.isScanning()) {
            device_count=0;
            permissions_granted = true;
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions_granted = false;
                requestLocationPermission();
            }

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissions_granted = false;
                requestBluetoothPermission();
            }

            if (permissions_granted) {
                Log.i(Constants.TAG, "Bluetooth permission has been granted");
                startScanning();
            }
        } else {
            showMsg(Utility.htmlColorGreen("Stopping scanning"));
            ble_scanner.stopScanning();
        }
    }

    public void doScanQR(View view) {
        //User has decided to scan a QR code
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i(Constants.TAG, "Doesn't have permission. Requesting permission");
            requestCameraPermission();
        } else {
            //User has permission
            startQRCodeActivity();
        }
    }

    public void doEnterCode(View view) {
        //User has decided to enter a dummy code
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Dummy code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();

                //Query the backend. Check if the code exists
                //Then get MacAddress
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();

    }

    private void startScanning() {
        if (permissions_granted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_device_list_adapter.clear();
                    ble_device_list_adapter.notifyDataSetChanged();
                }
            });
            simpleToast(getScanningMessage(),2000);
            ble_scanner.startScanning(this, SCAN_TIMEOUT);
        } else {
            showMsg(Utility.htmlColorRed("Permission to perform Bluetooth scanning was not yet granted"));
        }
    }

    private void startQRCodeActivity() {
        try {
            Intent intent = new Intent(MainActivity.this, ScanQR.class);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

            startActivityForResult(intent, MainSettingsActivity.START_MAIN_QR_SCAN);
        } catch (Exception e) {
            simpleToast("An error occurred while launching camera", Toast.LENGTH_LONG);
            System.out.println(e.getMessage());
        }
    }

    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void requestBluetoothPermission() {
        Log.i(Constants.TAG, "Bluetooth permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)){
            Log.i(Constants.TAG, "Displaying bluetooth permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Bluetooth access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH);
        }
    }

    private void requestCameraPermission() {
        Log.i(Constants.TAG, "Camera permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            Log.i(Constants.TAG, "Displaying Camera permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Camera access in order to scan the QR Code");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_LOCATION:
            case REQUEST_BLUETOOTH:
                Log.i(Constants.TAG, "Received response for permission request.");
                // Check if the only required permission has been granted
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                    if (!permissions_granted) {
                        Log.i(Constants.TAG, "Permission has now been granted. Check if it has all required scan permissions.");
                        permissions_granted = hasScanPermissions();
                        if (permissions_granted && ble_scanner.isScanning()) {
                            Log.i(Constants.TAG, "All required permissions granted & not scanning. Performing scan now.");
                            startScanning();
                        }
                    }
                }
            break;

            case REQUEST_CAMERA:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                    if (!permissions_granted) {
                        Log.i(Constants.TAG, "Permission has now been granted. Start camera activity");
                        startQRCodeActivity();
                    }
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    private boolean hasScanPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
    }

    private void generalAlert(String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setScanState(boolean value) {
        ble_scanning = value;
        ((Button) this.findViewById(R.id.scanButton)).setText(value ? Constants.STOP_SCANNING : "Find paired BBC micro:bits");
    }

    @Override
    public void candidateBleDevice(final BluetoothDevice device, byte[] scan_record, int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ble_device_list_adapter.addDevice(device);
                ble_device_list_adapter.notifyDataSetChanged();
                device_count++;
            }
        });
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
        showMsg(Utility.htmlColorGreen(getScanningMessage()));
    }

    @Override
    public void scanningStopped() {
        setScanState(false);
        if (device_count > 0) {
            showMsg(Utility.htmlColorGreen("Ready"));
        } else {
            showMsg(Utility.htmlColorRed(getNoneFoundMessage()));
        }
    }
    // adaptor
    private class ListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> ble_devices;

        public ListAdapter() {
            super();
            ble_devices = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            System.out.println("Added a bluetooth device");

            //API CALL HERE
            device.getAddress();

            if (!ble_devices.contains(device)) {
                ble_devices.add(device);
            }
        }

        public boolean contains(BluetoothDevice device) {
            return ble_devices.contains(device);
        }

        public BluetoothDevice getDevice(int position) {
            return ble_devices.get(position);
        }

        public void clear() {
            ble_devices.clear();
        }

        @Override
        public int getCount() {
            return ble_devices.size();
        }

        @Override
        public Object getItem(int i) {
            return ble_devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = MainActivity.this.getLayoutInflater().inflate(
                        R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) view.findViewById(R.id.textView);
                viewHolder.bdaddr = (TextView) view.findViewById(R.id.bdaddr);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = ble_devices.get(i);
            String deviceName = device.getName();
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                deviceName = deviceName + " (BONDED)";
            }
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.text.setText(deviceName);
            else
                viewHolder.text.setText("unknown device");

            viewHolder.bdaddr.setText(device.getAddress());

            return view;
        }
    }


    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    showMsg(Utility.htmlColorRed("Device was not paired successfully"));
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    showMsg(Utility.htmlColorGreen("Pairing is in progress"));
                } else {
                    //showMsg(Utility.htmlColorGreen("Device was paired successfully - select it now"));
                    StartCprTest(device);
                }
            }
        }
    };

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) MainActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    private String getScanningMessage() {
        if (Settings.getInstance().isFilter_unpaired_devices()) {
            return "Scanning for paired micro:bits";
        } else {
            return "Scanning for all micro:bits";

        }
    }

    private void setButtonText() {
        String text="";
        if (Settings.getInstance().isFilter_unpaired_devices()) {
            text = Constants.FIND_PAIRED;
        } else {
            text = Constants.FIND_ANY;
        }
        final String button_text = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) MainActivity.this.findViewById(R.id.scanButton)).setText(button_text);
            }
        });

    }

    private String getNoneFoundMessage() {
        if (Settings.getInstance().isFilter_unpaired_devices()) {
            return Constants.NO_PAIRED_FOUND;
        } else {
            return Constants.NONE_FOUND;
        }
    }

}
