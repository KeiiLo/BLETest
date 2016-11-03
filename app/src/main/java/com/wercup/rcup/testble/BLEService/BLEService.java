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
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.wercup.rcup.testble.MainActivity;

import java.util.UUID;

public class BLEService implements BluetoothAdapter.LeScanCallback {
    /**
     * Log/Debug
     */
    private static final String TAG = "BLEService";

    /**
     * Cheat
     */
    private static final String DEVICE_NAME = "Smart Sole 001";

    /**
     * All services/characteristics/descriptor UUIDs
     */
    /* Energy Service */
    private static final UUID ENERGY_SERVICE = UUID.fromString("00002300-1212-efde-1523-785fef13d123");
    private static final UUID ENERGY_DATA_CHAR = UUID.fromString("00002301-1212-efde-1523-785fef13d123");
    private static final UUID ENERGY_CONFIG_CHAR = UUID.fromString("00002302-1212-efde-1523-785fef13d123");
    /* Accelerometer Service */
    private static final UUID ACCEL_SERVICE = UUID.fromString("00002400-1212-efde-1523-785fef13d123");
    private static final UUID ACCEL_DATA_CHAR = UUID.fromString("00002401-1212-efde-1523-785fef13d123");
    private static final UUID ACCEL_CONFIG_CHAR = UUID.fromString("00002402-1212-efde-1523-785fef13d123");
    /* Step Counter Service */
    private static final UUID PRESSURE_SERVICE = UUID.fromString("00002500-1212-efde-1523-785fef13d123");
    private static final UUID PRESSURE_DATA_CHAR = UUID.fromString("00002501-1212-efde-1523-785fef13d123");
    private static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("00002502-1212-efde-1523-785fef13d123");
    /* Client Configuration Descriptor */
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * Instantiation des différents objets
     */

    // Attributs de la classe
    private BluetoothGatt mGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBuetoothLeScanner;
    private BluetoothDevice mDevice;
    private ScanFilter mFilter;
    private ScanCallback mScanCallBack;
    private BluetoothGattCharacteristic mCharacteristic;

    private SparseArray<BluetoothDevice> mDevices;
    // Instance de la classe
    private static BLEService mInstance;

    public boolean mEnabled;
    // Contexte de l'instance
    private Context mContext;

    public static BLEService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BLEService(context);
        }
        return mInstance;
    }

    public BLEService(Context context) {
        mContext = context;
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBuetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
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
                    } else {
                        Log.w(TAG, "Got accel config return value!");
                    }
                    break;
                case MSG_PRESSURE_CONFIG:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining pressure config return value");
                        return;
                    } else {
                        Log.w(TAG, "Got pressure config return value!");
                    }
                    break;
                case MSG_ENERGY_CONFIG:
                    mCharacteristic = (BluetoothGattCharacteristic) msg.obj;
                    trame = mCharacteristic.getValue();
                    if (trame == null) {
                        Log.w(TAG, "Error obtaining energy config return value");
                        return;
                    } else {
                        Log.w(TAG, "Got energy config return value!");
                    }
                    break;
                case MSG_PROGRESS:
//                    mProgress.setMessage((String) msg.obj);
//                    if (!mProgress.isShowing()) {
//                        mProgress.show();
//                    }
                    break;
                case MSG_DISMISS:
//                    mProgress.hide();
                    break;
                case MSG_CLEAR:
//                    clearDisplayValues();
                    break;
            }
        }
    };

    /**
     * Allow the scan to be stopped by making two threads
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
        mBluetoothAdapter.startLeScan(this);

//        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        if (mDevice == null) {
            Toast.makeText(mContext, "Couldn't find our device", Toast.LENGTH_LONG).show();
        }
        mBluetoothAdapter.stopLeScan(this);
//        mHandler.postDelayed(mConnectToDevice, 100);
        if (mDevice != null) {
            connectToDevice(mDevice);
        }
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getAddress() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if (DEVICE_NAME.equals(device.getName())) {
            Log.i(TAG, "Device found: " + device.getName() + ": " + device.getAddress().toString());
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

        /*
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
                /*case 1:
                    Log.d(TAG, "Set notify energy");
                    characteristic = gatt.getService(ENERGY_SERVICE)
                            .getCharacteristic(ENERGY_DATA_CHAR);
                    break;*/
                default:
                    mEnabled = true;
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
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
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
//                Camera2Fragment.getInstance().mConnectionState.setText("Disconnected");
//                Toast toast = Toast.makeText(Camera2Fragment.getInstance().getActivity(), "Disconnected", Toast.LENGTH_SHORT);
//                toast.show();
                mHandler.sendEmptyMessage(MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
//                Camera2Fragment.getInstance().mConnectionState.setText("Disconnected");

//                Toast toast = Toast.makeText(Camera2Fragment.getInstance().getActivity(), "Disconnected", Toast.LENGTH_SHORT);
//                toast.show();
                gatt.disconnect();
                gatt.close();
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
                Log.e("Accel", "Received Accel notification");
                mHandler.sendMessage(Message.obtain(null, MSG_ACCEL, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                Log.e("Pressure", "Received Pressure notification");
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
                /*byte[] setConfig = new byte[]{(byte) 0x30, (byte) 0x28, (byte) 0x00};
                byte[] readConfig = new byte[]{(byte) 0x31};
                BluetoothGattCharacteristic configChar = mGatt.getService(PRESSURE_SERVICE).getCharacteristic(PRESSURE_CONFIG_CHAR);
                if (count % 2 == 0) {
                    configChar.setValue(setConfig);
                } else {
                    configChar.setValue(readConfig);
                }
                count++;
                gatt.writeCharacteristic(configChar);*/
            }
            if (ENERGY_DATA_CHAR.equals(characteristic.getUuid())) {
                Log.e("Energy", "Received Energy notification");
                mHandler.sendMessage(Message.obtain(null, MSG_ENERGY, characteristic));
            }
            if (ENERGY_CONFIG_CHAR.equals(characteristic.getUuid())) {
//                Log.e("Energy", "Received Energy notification");
                mHandler.sendMessage(Message.obtain(null, MSG_ENERGY_CONFIG, characteristic));
            }
            if (PRESSURE_CONFIG_CHAR.equals(characteristic.getUuid())) {
//                Log.e("Energy", "Received Energy notification");
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CONFIG, characteristic));
            }
            if (ACCEL_CONFIG_CHAR.equals(characteristic.getUuid())) {
//                Log.e("Energy", "Received Energy notification");
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

}