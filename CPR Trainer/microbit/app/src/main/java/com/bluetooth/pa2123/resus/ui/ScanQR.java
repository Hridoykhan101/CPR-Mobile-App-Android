package com.bluetooth.pa2123.resus.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.bluetooth.pa2123.resus.R;
import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.ScanMode;

public class ScanQR extends AppCompatActivity {

    private CodeScanner codeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        initializeCodeScanner();
    }

    private void initializeCodeScanner() {
        codeScanner = new CodeScanner(this, this.findViewById(R.id.scannerView));

        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(CodeScanner.TWO_DIMENSIONAL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setScanMode(ScanMode.CONTINUOUS);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);

        codeScanner.setDecodeCallback( result -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Get our scan result, return this value
                    Intent intent = getIntent();
                    intent.putExtra("SCAN_RESULT", result.getText());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        });

        codeScanner.startPreview();
    }
}