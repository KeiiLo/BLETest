package com.wercup.rcup.testble;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wercup.rcup.testble.BLEService.BLEService;
import com.wercup.rcup.testble.fragments.MainFragment;
import com.wercup.rcup.testble.tools.SensorTagData;

import java.util.UUID;

/**
 * Created by KeiLo on 22/09/16.
 */
public class MainActivity extends Activity {
    private static final String TAG = "BluetoothGattActivity";

    private ProgressDialog mProgress;


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BLEService mBLEService;
    private static MainFragment mainFragment;
    private static Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        mainFragment = MainFragment.getInstance();
        currentFragment = mainFragment;
        fm.beginTransaction().add(R.id.content_main, mainFragment).addToBackStack(null).commit();

        /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);

        // Android M Permission check 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }

        mBLEService = BLEService.getInstance(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (BLEService.getInstance(this).getmBluetoothAdapter() == null || !BLEService.getInstance(this).getmBluetoothAdapter().isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
//            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_SERVICE);
        this.registerReceiver(dataReceiver, filter);
        if (mainFragment != null && currentFragment == mainFragment) {
            mainFragment.clearDisplayValues();
        }
//        if (mConnectedGatt != null) {
//            mConnectedGatt.connect();
//        }
    /*
        Log.e("Device Bonded",String.valueOf(mConnectedGatt));
        Log.e("Device Bonded",String.valueOf(mConnectedGatt.getDevice()));
        Log.e("Device Bonded",String.valueOf(mConnectedGatt.getDevice().getBondState()));
        */

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Make sure dialog is hidden
//        mProgress.dismiss();
        //Cancel any scans in progress
        mBLEService.getmHandler().removeCallbacks(mBLEService.getmStopRunnable());
        mBLEService.getmHandler().removeCallbacks(mBLEService.getmStartRunnable());
        try {
            this.unregisterReceiver(dataReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
//        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        //Disconnect from any active tag connection
        if (BLEService.getInstance(this).getmGatt() != null) {
            Log.e("onStop", "disconnecting from " + BLEService.getInstance(this).getmGatt().getDevice().getName());
            //mConnectedGatt.disconnect();
//            mConnectedGatt = null;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.main, menu);
        //Add any device elements we've discovered to the overflow menu
        for (int i = 0; i < mBLEService.getmDevices().size(); i++) {
            BluetoothDevice device = mBLEService.getmDevices().valueAt(i);
            menu.add(0, mBLEService.getmDevices().keyAt(i), 0, device.getName());
        }

        return true;
    }

    private Runnable deviceNames = new Runnable() {
        @Override
        public void run() {
            if (mBLEService.getmDevices() != null) {
                Log.e(TAG, "Why the fuck is there a device?");
            } else {
                Log.e(TAG, "Shit, no device.");
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mBLEService.getmDevices().clear();
                mBLEService.startScan();
                mHandler.postDelayed(deviceNames, 2500);
                IntentFilter filter = new IntentFilter();
                filter.addAction(NOTIFICATION_SERVICE);
                this.registerReceiver(dataReceiver, filter);
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mBLEService.getmDevices().get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
                if (mBLEService.getmGatt() == null) {
                    mBLEService.setmGatt(device.connectGatt(this, true, mBLEService.getmGattCallback()));
                } else {
                    mHandler.sendMessage(Message.obtain(null, MSG_DISMISS, "Connected"));
                }

                //Display progress UI
                return super.onOptionsItemSelected(item);
        }
    }

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mainFragment != null && currentFragment == mainFragment) {
                BluetoothGattCharacteristic accelCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("00000001-1212-efde-1523-785fef13d123"), BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ);
                BluetoothGattCharacteristic pressureCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("00000002-1212-efde-1523-785fef13d123"), BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ);
                ;
                BluetoothGattCharacteristic energyCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("00000003-1212-efde-1523-785fef13d123"), BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ);
                ;

                accelCharacteristic.setValue(intent.getByteArrayExtra("accel"));
                pressureCharacteristic.setValue(intent.getByteArrayExtra("pressure"));
                energyCharacteristic.setValue(intent.getByteArrayExtra("energy"));

//            Log.e(TAG, MainActivity.byteToHex(accelCharacteristic.getValue()));
//            Log.e(TAG, MainActivity.byteToHex(pressureCharacteristic.getValue()));
//            Log.e(TAG, MainActivity.byteToHex(energyCharacteristic.getValue()));
                if (accelCharacteristic.getValue() != null) {
                    updateAccelValues(accelCharacteristic);
                } else if (pressureCharacteristic.getValue() != null) {
                    updatePressureValue(pressureCharacteristic);
                } else if (energyCharacteristic.getValue() != null) {
                    updateEnergyValue(energyCharacteristic);
                }
            }
        }
    };
    /**
     * We have a Handler to process event results on the main thread
     **/
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 203;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case MSG_PROGRESS:
                    mProgress.setMessage((String) msg.obj);
                    if (!mProgress.isShowing()) {
                        mProgress.show();
                    }
                    break;
                case MSG_DISMISS:
                    mProgress.hide();
                    break;
                case MSG_CLEAR:
                    mainFragment.clearDisplayValues();
                    break;
            }
        }
    };

    /* Methods to extract sensor data and update the UI */

    public static void updateAccelValues(BluetoothGattCharacteristic characteristic) {
        int[] values;
        values = SensorTagData.extractAccelCoefficients(characteristic);
        mainFragment.mXAccel.setText(String.valueOf(values[0]));
        mainFragment.mYAccel.setText(String.valueOf(values[1]));
        mainFragment.mZAccel.setText(String.valueOf(values[2]));
        mainFragment.mTemp.setText(String.valueOf(values[3]));
    }

    public static void updateEnergyValue(BluetoothGattCharacteristic characteristic) {
        double[] batteryLevel = SensorTagData.getBatteryLevel(characteristic);
        mainFragment.mBattery.setText(String.valueOf(batteryLevel[0]) + "mA");
        mainFragment.mBattery2.setText(String.valueOf(batteryLevel[1]) + "V");
    }


    public static void updatePressureValue(BluetoothGattCharacteristic characteristic) {
        if (SensorTagData.isTapTap(characteristic)) {
            mainFragment.mTapTap.setText("True");
        } else {
            mainFragment.mTapTap.setText("False");
        }
        int steps = SensorTagData.getSteps(characteristic);
        mainFragment.mStep.setText(String.valueOf(steps));
    }

    public static String byteToHex(byte[] value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }


    @Override
    public void onStart() {
        super.onStart();
    }
}
