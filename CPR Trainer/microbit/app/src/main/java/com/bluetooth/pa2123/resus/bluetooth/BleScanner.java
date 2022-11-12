package com.bluetooth.pa2123.resus.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.bluetooth.pa2123.resus.Constants;

abstract public class BleScanner {

    BluetoothAdapter bluetooth_adapter = null;
    Handler handler = new Handler();
    ScanResultsConsumer scan_results_consumer;
    Context context;
    boolean scanning=false;
    String device_name_start="";
    boolean select_bonded_devices_only=true;

    public BleScanner(Context context) {
        this.context = context;

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetooth_adapter = bluetoothManager.getAdapter();

        // check bluetooth is available and on
        if (bluetooth_adapter == null || !bluetooth_adapter.isEnabled()) {
            Log.d(Constants.TAG, "Bluetooth is NOT switched on");
            Intent enableBtIntent = new Intent(	BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
        }
        Log.d(Constants.TAG, "Bluetooth is switched on");
    }

    abstract public void startScanning(ScanResultsConsumer scan_results_consumer);
    abstract public void startScanning(ScanResultsConsumer scan_results_consumer, long stop_after_ms);
    abstract public void stopScanning();

    public boolean isScanning() {
        return scanning;
    }

    void setScanning(boolean scanning) {
        this.scanning = scanning;
        if (!scanning) {
            scan_results_consumer.scanningStopped();
        } else {
            scan_results_consumer.scanningStarted();        }
    }

    public String getDevice_name_start() {
        return device_name_start;
    }

    public void setDevice_name_start(String device_name_start) {
        this.device_name_start = device_name_start;
    }

    public boolean isSelect_bonded_devices_only() {
        return select_bonded_devices_only;
    }

    public void setSelect_bonded_devices_only(boolean select_bonded_devices_only) {
        this.select_bonded_devices_only = select_bonded_devices_only;
    }

}
