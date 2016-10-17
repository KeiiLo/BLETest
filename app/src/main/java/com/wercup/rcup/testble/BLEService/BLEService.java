//package com.wercup.rcup.testble.BLEService;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Message;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.SparseArray;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.wercup.rcup.testble.MainActivity;
//import com.wercup.rcup.testble.tools.SensorTagData;
//
//import java.util.List;
//import java.util.UUID;
//
//import static android.content.ContentValues.TAG;
//import static android.content.Context.BLUETOOTH_SERVICE;
//
///**
// * Created by KeiLo on 22/09/16.
// */
//public class BLEService implements BluetoothAdapter.LeScanCallback {
//    private static final String TAG = "BLEService";
//
//    private static final String DEVICE_NAME = "Smart Sole 001";
//
//    /* Energy Service */
//    private static final UUID ENERGY_SERVICE = UUID.fromString("00002300-1212-efde-1523-785fef13d123");
//    private static final UUID ENERGY_DATA_CHAR = UUID.fromString("00002301-1212-efde-1523-785fef13d123");
//    private static final UUID ENERGY_CONFIG_CHAR = UUID.fromString("00002302-1212-efde-1523-785fef13d123");
//    /* Accelerometer Service */
//    private static final UUID ACCEL_SERVICE = UUID.fromString("00002400-1212-efde-1523-785fef13d123");
//    private static final UUID ACCEL_DATA_CHAR = UUID.fromString("00002401-1212-efde-1523-785fef13d123");
//    private static final UUID ACCEL_CONFIG_CHAR = UUID.fromString("00002402-1212-efde-1523-785fef13d123");
//    /* Step Counter Service */
//    private static final UUID PRESSURE_SERVICE = UUID.fromString("00002500-1212-efde-1523-785fef13d123");
//    private static final UUID PRESSURE_DATA_CHAR = UUID.fromString("00002501-1212-efde-1523-785fef13d123");
//    private static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("00002502-1212-efde-1523-785fef13d123");
//    /* Client Configuration Descriptor */
//    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//
//
//    public BluetoothAdapter mBluetoothAdapter;
//    public BluetoothLeScanner mBluetoothLeScanner;
//    private SparseArray<BluetoothDevice> mDevices;
//    private BluetoothGattCharacteristic mCharacteristic;
//
//    private BluetoothGatt mConnectedGatt;
//
//    private ProgressDialog mProgress;
//
//    private Activity context;
//
//    public BLEService(Context context) {
//
//        this.context = (Activity) context;
//        BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
//        mBluetoothAdapter = manager.getAdapter();
//        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//
//        mDevices = new SparseArray<>();
//    }
//
//    public Runnable mStopRunnable = new Runnable() {
//        @Override
//        public void run() {
//            stopScan();
//        }
//    };
//    public Runnable mStartRunnable = new Runnable() {
//        @Override
//        public void run() {
//            startScan();
//        }
//    };
//    public void startScan() {
////        ScanFilter soleFilter = new ScanFilter.Builder().setServiceUuid(PRESSURE_SERVICE).build();
////        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
////        filters.add(soleFilter);
//
////        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
////        mBluetoothLeScanner.startScan(filters, setting);
//        mBluetoothAdapter.startLeScan(this);
//
//        mHandler.postDelayed(mStopRunnable, 2500);
//    }
//
//    public void stopScan() {
//        mBluetoothAdapter.stopLeScan(this);
//    }
//    @Override
//    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        Log.i(TAG, "New LE Device: " + device.getAddress() + " @ " + rssi);
//        /*
//         * We are looking for SensorTag devices only, so validate the name
//         * that each device reports before adding it to our collection
//         */
//        if (DEVICE_NAME.equals(device.getName())) {
//            mDevices.put(device.hashCode(), device);
//            //Update the overflow menu
//        }
//    }
//
//    /**
//     * We have a Handler to process event results on the main thread
//     **/
//    private static final int MSG_ENERGY = 301;
//    private static final int MSG_ENERGY_CONFIG = 302;
//    private static final int MSG_ACCEL = 401;
//    private static final int MSG_ACCEL_CONFIG = 402;
//    private static final int MSG_PRESSURE = 501;
//    private static final int MSG_PRESSURE_CONFIG = 502;
//    private static final int MSG_PROGRESS = 201;
//    private static final int MSG_DISMISS = 202;
//    private static final int MSG_CLEAR = 203;
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            BluetoothGattCharacteristic characteristic;
//            switch (msg.what) {
//                case MSG_ACCEL:
//                    characteristic = (BluetoothGattCharacteristic) msg.obj;
//                    if (characteristic.getValue() == null) {
//                        Log.w(TAG, "Error obtaining accel value");
//                        return;
//                    }
////                    updateAccelValues(characteristic);
//                    break;
//                case MSG_PRESSURE:
//                    characteristic = (BluetoothGattCharacteristic) msg.obj;
//                    if (characteristic.getValue() == null) {
//                        Log.w(TAG, "Error obtaining pressure value");
//                        return;
//                    }
////                    updatePressureValue(characteristic);
//                    break;
//                case MSG_ENERGY:
//                    characteristic = (BluetoothGattCharacteristic) msg.obj;
//                    if (characteristic.getValue() == null) {
//                        Log.w(TAG, "Error obtaining energy value");
//                        return;
//                    }
////                    updateEnergyValue(characteristic);
//                    break;
//                case MSG_ACCEL_CONFIG:
//                    characteristic = (BluetoothGattCharacteristic) msg.obj;
//                    if (characteristic.getValue() == null) {
//                        Log.w(TAG, "Error obtaining accel config return value");
//                        return;
//                    }
//                    break;
//                case MSG_PRESSURE_CONFIG:
//                    characteristic = (BluetoothGattCharacteristic) msg.obj;
//                    if (characteristic.getValue() == null) {
//                        Log.w(TAG, "Error obtaining pressure config return value");
//                        return;
//                    }
//                    break;
//                case MSG_ENERGY_CONFIG:
//                    characteristic = (BluetoothGattCharacteristic) msg.obj;
//                    if (characteristic.getValue() == null) {
//                        Log.w(TAG, "Error obtaining energy config return value");
//                        return;
//                    }
//                    else
//                    {
//                        Log.w(TAG, "Got Energy config return value!");
//                    }
//                    break;
//                case MSG_PROGRESS:
////                    mProgress.setMessage((String) msg.obj);
////                    if (!mProgress.isShowing()) {
////                        mProgress.show();
////                    }
//                    break;
//                case MSG_DISMISS:
////                    mProgress.hide();
//                    break;
//                case MSG_CLEAR:
////                    clearDisplayValues();
//                    break;
//            }
//        }
//    };
//}