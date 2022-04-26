package com.ee5.mobile.SupportClasses.Ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ee5.mobile.SupportClasses.IFrameBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleService extends Service {
    private final static String TAG = "BleService";
    private static final boolean AUTO_CONNECT = false;
    private final IBinder mBinder = new LocalBinder();
    private int dataSequence = 0;
    private BluetoothSocket mSocket;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private Handler handler = new Handler();
    private Boolean isConnected;

    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public boolean init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "init: BluetoothAdapter not obtained");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || !BluetoothAdapter.checkBluetoothAddress(address)) {
            Log.e(TAG, "connect: BleAdapter not initialized or incorrect address");
            return false;
        }

        try {
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mBluetoothGatt = device.connectGatt(this, AUTO_CONNECT, mGattCallback);
        } catch (IllegalArgumentException e){
            Log.w(TAG, "connect: Device not found with provided address");
        }
        return true;
    }

    public Boolean isConnected() {
        return isConnected;
    }

    public String getConnectedAddress(){
        return mBluetoothDeviceAddress;
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothDeviceAddress = null;
        isConnected = false;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private void connectSocket(BluetoothDevice device) {
        if (device == null) Log.e(TAG, "wifiProvisionDevice: device is null");
        device.createBond();
        Log.d(TAG, "connectSocket: " + device.getBondState());

        mBluetoothAdapter.cancelDiscovery();

        UUID mUuid = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");


        try {
            mSocket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class)
                                                            .invoke(device,mUuid);
            assert mSocket != null;
            mSocket.connect();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }
        Log.d(TAG, "Socket state connected: " + mSocket.isConnected());
    }

    public Boolean wifiProvisionDevice(BluetoothDevice device, byte[] ssid, byte[] pass) {
        Log.d(TAG, "wifiProvisionDevice: provision to " + device.getAddress());

        dataSequence +=1;
        byte[] ssidFrame = IFrameBuilder.getSsidDataFrame(ssid, dataSequence);
        connectSocket(device);

        return true;
    }
}
