package com.ee5.mobile.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;


import com.ee5.mobile.R;

import java.util.Set;


public class SetupActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0x01;

    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bleScanner;
    private ScanCallback bleCallBack;
    private BluetoothDevice bleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

        bleAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bleAdapter != null){
            System.out.println("bleAdapter not null");
        }

        bleScanner = bleAdapter.getBluetoothLeScanner();

        bleCallBack = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                bleDevice = result.getDevice();
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                System.out.println("scan failed");
            }

            
        };

        bleScanner.startScan(bleCallBack);

        String scannedAddress = bleDevice.getAddress();
        System.out.println(scannedAddress);


        //Set<BluetoothDevice> BondedSet = bleAdapter.getBondedDevices(); //Returns set of all devices my phone remembers

        /*BondedSet.stream()
                .forEach(bluetoothDevice -> Log.d("Bluetooth",bluetoothDevice.getAddress()));*/

        String espAddress = "C4:DD:57:9E:88:40";

        //BluetoothDevice device = bleAdapter.getRemoteDevice();

        //BlufiClient client = new BlufiClient(this, device);
    }

}
