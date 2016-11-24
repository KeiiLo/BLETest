package com.wercup.rcup.testble.BLEService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.wercup.rcup.testble.MainActivity;
import com.wercup.rcup.testble.tools.SensorTagData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by KeiLo on 14/09/16.
 */

public class BLEService implements BluetoothAdapter.LeScanCallback {
    /**
     * Log/Debug
     */
    private static final String TAG = "BLEService";

    /**
     * Cheat
     */
    private static final String DEVICE_NAME = "Smart Sole 001";
    private static final String DEVICE_MAC = "F9:B6:E9:FD:2A:98";

    /**
     * All services/characteristics/descriptor UUIDs
     */
    /* Energy Service */
    public static final UUID ENERGY_SERVICE = UUID.fromString("00002300-1212-efde-1523-785fef13d123");
    public static final UUID ENERGY_DATA_CHAR = UUID.fromString("00002301-1212-efde-1523-785fef13d123");
    public static final UUID ENERGY_CONFIG_CHAR = UUID.fromString("00002302-1212-efde-1523-785fef13d123");
    /* Accelerometer Service */
    public static final UUID ACCEL_SERVICE = UUID.fromString("00002400-1212-efde-1523-785fef13d123");
    public static final UUID ACCEL_DATA_CHAR = UUID.fromString("00002401-1212-efde-1523-785fef13d123");
    public static final UUID ACCEL_CONFIG_CHAR = UUID.fromString("00002402-1212-efde-1523-785fef13d123");
    /* Step Counter Service */
    public static final UUID PRESSURE_SERVICE = UUID.fromString("00002500-1212-efde-1523-785fef13d123");
    public static final UUID PRESSURE_DATA_CHAR = UUID.fromString("00002501-1212-efde-1523-785fef13d123");
    public static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("00002502-1212-efde-1523-785fef13d123");
    /* Client Configuration Descriptor */
    public static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    /**
     * Instantiation des différents objets
     */

    // Tous les attributs correspondant au BLE
    private BluetoothGatt mGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothDevice mDevice;
    private BluetoothGattCharacteristic mCharacteristic;
    private ScanCallback scanCallBack;

    private SparseArray<BluetoothDevice> mDevices;

    // Instance de la classe
    private static BLEService mInstance;

    // Booléen de garde pour vérifier si tous les services sont activés
    public boolean mEnabled;

    // Contexte de l'instance
    private Context mContext;

    /**
     * Simple fonction pour créer une instance ou la récupérer si elle existe déjà
     */
    public static BLEService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BLEService(context);
        }
        return mInstance;
    }

    /**
     * Constructeur
     */
    public BLEService(Context context) {
        mContext = context;
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mDevices = new SparseArray<>();
    }

    public BluetoothGattCharacteristic getmCharacteristic() {
        return mCharacteristic;
    }

    public BluetoothGatt getmGatt() {
        return mGatt;
    }

    public void setmGatt(BluetoothGatt mGatt) {
        this.mGatt = mGatt;
    }

    public BluetoothManager getmBluetoothManager() {
        return mBluetoothManager;
    }

    public void setmBluetoothManager(BluetoothManager mBluetoothManager) {
        this.mBluetoothManager = mBluetoothManager;
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    public BluetoothDevice getmDevice() {
        return mDevice;
    }

    public void setmDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public SparseArray<BluetoothDevice> getmDevices() {
        return mDevices;
    }

    public void setmDevices(SparseArray<BluetoothDevice> mDevices) {
        this.mDevices = mDevices;
    }

    public Runnable getmStopRunnable() {
        return mStopRunnable;
    }

    public Runnable getmStartRunnable() {
        return mStartRunnable;
    }

    public BluetoothGattCallback getmGattCallback() {
        return mGattCallback;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    /**
     * Handler to process multiple events on the main thread
     **/
    private static final int MSG_ENERGY = 301;
    private static final int MSG_ENERGY_CONFIG = 302;
    private static final int MSG_ACCEL = 401;
    private static final int MSG_ACCEL_CONFIG = 402;
    private static final int MSG_PRESSURE = 501;
    private static final int MSG_PRESSURE_CONFIG = 502;
    private static final int MSG_STATE = 200;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 203;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] trame;
            switch (msg.what) {
                case MSG_ACCEL:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining accel value");
                        return;
                    } else if (mEnabled) {
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                        mContext.sendBroadcast(intent.putExtra("accel", trame));
                    }
                    break;
                case MSG_PRESSURE:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining pressure value");
                        return;
                    } else if (mEnabled) {
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                        mContext.sendBroadcast(intent.putExtra("pressure", trame));
                    }
                    break;
                case MSG_ENERGY:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining energy value");
                        return;
                    } else if (mEnabled) {
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                        mContext.sendBroadcast(intent.putExtra("energy", trame));
                    }
                    break;
                case MSG_ACCEL_CONFIG:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining accel config return value");
                        return;
                    } else if (mEnabled) {
                        Log.i(TAG, "Response From Accel" + SensorTagData.bytesToHex(trame));
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                        mContext.sendBroadcast(intent.putExtra("accelconfig", trame));
                    }
                    break;
                case MSG_PRESSURE_CONFIG:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining pressure config return value");
                        return;
                    } else if (mEnabled) {
                        Log.i(TAG, "Response From Pressure" + SensorTagData.bytesToHex(trame));
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                        Log.e(TAG, "Pressure config is :" + SensorTagData.bytesToHex(trame));
                        mContext.sendBroadcast(intent.putExtra("pressureconfig", trame));
                    }
                    break;
                case MSG_ENERGY_CONFIG:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining energy config return value");
                        return;
                    } else if (mEnabled) {
                        Log.i(TAG, "Response From Energy" + SensorTagData.bytesToHex(trame));
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                        mContext.sendBroadcast(intent.putExtra("energyconfig", trame));
                    }
                    break;
                case MSG_STATE:
                    boolean connected = (boolean) msg.obj;
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.NOTIFICATION_SERVICE);
                    mContext.sendBroadcast(intent.putExtra("state", connected));

                case MSG_PROGRESS:
//                    mProgress.setMessage((String) msg.obj);
//                    if (!mProgress.isShowing()) {
//                        mProgress.show();
//                    }
                    break;
                case MSG_DISMISS:
                    readEnergyConfig(getmGatt());
                    break;
                case MSG_CLEAR:
//                    clearDisplayValues();
                    break;
            }
        }
    };

    /**
     * Allow the scan to be stopped or started by making two threads
     */

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private Runnable mConnectToDevice = new Runnable() {
        @Override
        public void run() {
            connectToDevice(mDevice);
        }
    };

    public void startScan() {
        Log.d(TAG, "Scanning devices");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "start le scan");
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .build();
            ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
            filters.add(scanFilter);

            Log.e(TAG, "Scanner 21");
            /*
            ATTENTION au mode de scan qui peut jouer sur la découverte ou non de certains appareils
             */
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            scanCallBack = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    processResult(result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };

            mBluetoothLeScanner.startScan(filters, settings, scanCallBack);
        } else {
            mBluetoothAdapter.startLeScan(this);
        }
//        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void processResult(ScanResult result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "process result");
            /*if (result.getDevice() != null && result.getDevice().getName() != null) {
                if (result.getDevice().getAddress().toString().equals(DEVICE_MAC)) {
                    Log.e(TAG, "User's Mac Address is:" + DEVICE_MAC);
                    stopScan();
                    connectToDevice(mDevice);
                }
            }*/
            mDevice = result.getDevice();
            Log.e(TAG, "Device found: " + mDevice.getName() + ": " + mDevice.getAddress().toString());
            if (result.getDevice() != null) {
                mDevices.put(mDevice.hashCode(), mDevice);
            }
        }
    }

    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner.stopScan(scanCallBack);
        } else {
            mBluetoothAdapter.stopLeScan(this);
        }
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getAddress() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if (DEVICE_MAC.equals(device.getAddress())) {
            Log.e(TAG, "User's Mac Address is:" + DEVICE_MAC);
            Log.e(TAG, "Device found: " + device.getName() + ": " + device.getAddress().toString());
            mDevices.put(device.hashCode(), device);
            mDevice = device;
            //Update the overflow menu
            stopScan();
        }
    }

    public BluetoothGatt connectToDevice(BluetoothDevice device) {
        Toast.makeText(mContext, "Connecting to " + mDevice.getName(), Toast.LENGTH_SHORT).show();
        if (mGatt == null) {
            mGatt = device.connectGatt(mContext, true, mGattCallback);
        }
        Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
        return mGatt;
    }


    public BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        private int mState = 0;

        private void reset() {
            mState = 0;
        }

        private void advance() {
            Log.e("Advance", "Called ! mState is " + mState);
            mState++;
        }

        /**
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            mEnabled = false;
            BluetoothGattCharacteristic characteristic;
            Log.e("mState", String.valueOf(mState));
            switch (mState) {
                case 0:
                    Log.d(TAG, "Set notify pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Set notify accel");
                    characteristic = gatt.getService(ACCEL_SERVICE)
                            .getCharacteristic(ACCEL_DATA_CHAR);
                    break;
                case 2:
                    Log.d(TAG, "Set notify energy");
                    characteristic = gatt.getService(ENERGY_SERVICE)
                            .getCharacteristic(ENERGY_DATA_CHAR);
                    break;
                case 3:
                    Log.d(TAG, "Enabling pressure config");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    break;
                case 4:
                    Log.d(TAG, "Enabling accel config");
                    characteristic = gatt.getService(ACCEL_SERVICE)
                            .getCharacteristic(ACCEL_CONFIG_CHAR);
                    break;
                case 5:
                    Log.d(TAG, "Enabling energy config");
                    characteristic = gatt.getService(ENERGY_SERVICE)
                            .getCharacteristic(ENERGY_CONFIG_CHAR);
                    break;
                default:
                    mEnabled = true;
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);

            // Enable remote indication -> Mainly for settings purpose
            if (characteristic.getUuid().equals(ENERGY_CONFIG_CHAR) || characteristic.getUuid().equals(ACCEL_CONFIG_CHAR) || characteristic.getUuid().equals(PRESSURE_CONFIG_CHAR)) {
                Log.e("BLE", "Indication enabled on Energy config");
                BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
                desc.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                gatt.writeDescriptor(desc);
            }
            // Enable remote notifications -> listening to the data that is broadcast by the Device
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: " + status + " -> " + connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
//                Camera2Fragment.getInstance().mConnectionState.setText("Connected");
                gatt.discoverServices();
//                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
                mHandler.sendMessage(Message.obtain(null, MSG_STATE, true));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                Log.e(TAG, "Lost device");
                startScan();
//                mHandler.sendEmptyMessage(MSG_CLEAR);
                mHandler.sendMessage(Message.obtain(null, MSG_STATE, false));
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
//                Camera2Fragment.getInstance().mConnectionState.setText("Disconnected");

//                Toast toast = Toast.makeText(Camera2Fragment.getInstance().getActivity(), "Disconnected", Toast.LENGTH_SHORT);
//                toast.show();
                Log.e(TAG, "Unknown error");
//                gatt.disconnect();
//                gatt.close();
                mHandler.sendMessage(Message.obtain(null, MSG_STATE, false));

                startScan();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: " + status);
            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            reset();
            setNotifyNextSensor(gatt);
        }

        private int count = 0;

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            if (ACCEL_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCEL, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            if (ENERGY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ENERGY, characteristic));
            }
            if (ENERGY_CONFIG_CHAR.equals(characteristic.getUuid())) {
                Log.e("Energy", "Received Energy config response: " + SensorTagData.bytesToHex(characteristic.getValue()));
                mHandler.sendMessage(Message.obtain(null, MSG_ENERGY_CONFIG, characteristic));
            }
            if (PRESSURE_CONFIG_CHAR.equals(characteristic.getUuid())) {
                Log.e("Energy", "Received Pressure config response: " + SensorTagData.bytesToHex(characteristic.getValue()));
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CONFIG, characteristic));
            }
            if (ACCEL_CONFIG_CHAR.equals(characteristic.getUuid())) {
                Log.e("Energy", "Received Accel config response: " + SensorTagData.bytesToHex(characteristic.getValue()));
                mHandler.sendMessage(Message.obtain(null, MSG_ACCEL_CONFIG, characteristic));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            advance();
            setNotifyNextSensor(gatt);
        }
    };

    private String connectionState(int status) {
        switch (status) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting";
            default:
                return String.valueOf(status);
        }
    }

    final protected char[] hexArray = "0123456789ABCDEF".toCharArray();

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void readEnergyConfig(BluetoothGatt gatt) {
        byte[] readConfig = new byte[]{(byte) 0x11};
        BluetoothGattCharacteristic readConfigChar = gatt.getService(BLEService.ENERGY_SERVICE).getCharacteristic(BLEService.ENERGY_CONFIG_CHAR);
        readConfigChar.setValue(readConfig);
        gatt.writeCharacteristic(readConfigChar);
    }

    public static void readAccelConfig(BluetoothGatt gatt) {
        byte[] readConfig = new byte[]{(byte) 0x21};
        BluetoothGattCharacteristic readConfigChar = gatt.getService(BLEService.ACCEL_SERVICE).getCharacteristic(BLEService.ACCEL_CONFIG_CHAR);
        readConfigChar.setValue(readConfig);
        gatt.writeCharacteristic(readConfigChar);
    }

    public static void readPressureConfig(BluetoothGatt gatt) {
        byte[] readConfig = new byte[]{(byte) 0x31};
        BluetoothGattCharacteristic readConfigChar = gatt.getService(BLEService.PRESSURE_SERVICE).getCharacteristic(BLEService.PRESSURE_CONFIG_CHAR);
        readConfigChar.setValue(readConfig);
        gatt.writeCharacteristic(readConfigChar);
    }

    public static void sendEnergyConfig(BluetoothGatt gatt) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(BLESettings.getEnergyRefreshRate());
        byte[] result = b.array();
        byte[] sendConfig = new byte[] {(byte) 0x10, result[2], result[3]};
        Log.i("Tete", "Send Energy " + SensorTagData.bytesToHex(sendConfig));
        BluetoothGattCharacteristic sendConfigChar = gatt.getService(BLEService.ENERGY_SERVICE).getCharacteristic(BLEService.ENERGY_CONFIG_CHAR);
        sendConfigChar.setValue(sendConfig);
        gatt.writeCharacteristic(sendConfigChar);
    }

    private static byte[] prepAccelConfig() {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(BLESettings.getAccelRefreshRate());
        byte[] result = b.array();
        int firstByte = (BLESettings.getBandwidth() << 2) + BLESettings.getFullScaleSelection();
        int secondByte = (BLESettings.getxAxis() << 5) + (BLESettings.getxAxis() << 4) + (BLESettings.getxAxis() << 3) + BLESettings.getAccelOutputRate();

        byte[] sendConfig = new byte[] {(byte) firstByte, (byte) secondByte, result[2], result[3]};
        return sendConfig;
    }

    public static void sendAccelConfig(BluetoothGatt gatt) {

        byte[] result = prepAccelConfig();
        byte[] sendConfig = new byte[] {(byte) 0x20, result[0], result[1], result[2], result[3]};
        Log.i("Tete", "Send Accel " + SensorTagData.bytesToHex(sendConfig));
        BluetoothGattCharacteristic sendConfigChar = gatt.getService(BLEService.ACCEL_SERVICE).getCharacteristic(BLEService.ACCEL_CONFIG_CHAR);
        sendConfigChar.setValue(sendConfig);
        gatt.writeCharacteristic(sendConfigChar);
    }

    private static byte[] prepPressureConfig() {
        int lowerByte = (BLESettings.getLCOMPInput() << 5) + (BLESettings.getComparatorThres() << 1) + BLESettings.getEnableHyst();
        int upperByte = (BLESettings.getResetCounter() << 5) + (BLESettings.getReadyEvent() << 4) + (BLESettings.getDownEvent() << 3) +
                (BLESettings.getUpEvent() << 2) + (BLESettings.getCrossEvent() << 1) + BLESettings.getLCOMPState();
        return new byte[] {(byte) lowerByte, (byte) upperByte};
    }
    public static void sendPressureConfig(BluetoothGatt gatt) {
        byte[] result = prepPressureConfig();
        BLESettings.setResetCounter(0);
        byte[] sendConfig = new byte[] {(byte) 0x30, result[0], result[1]};
        Log.i("Tete", "Send Pressure " + SensorTagData.bytesToHex(sendConfig));
        BluetoothGattCharacteristic sendConfigChar = gatt.getService(BLEService.PRESSURE_SERVICE).getCharacteristic(BLEService.PRESSURE_CONFIG_CHAR);
        sendConfigChar.setValue(sendConfig);
        gatt.writeCharacteristic(sendConfigChar);
    }
}
