package com.wercup.rcup.testble;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wercup.rcup.testble.BLEService.BLEService;
import com.wercup.rcup.testble.tools.SensorTagData;
import com.wercup.rcup.testble.fragments.mainFragment;

import java.util.List;
import java.util.UUID;

/**
 * Created by KeiLo on 22/09/16.
 */
public class MainActivity extends Activity {
    private static final String TAG = "BluetoothGattActivity";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static mainFragment mainFragment;
    private static Fragment currentFragment = null;

    private BLEService mBLEService;

    public BLEService getmBLEService() {
        return mBLEService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        FragmentManager fm = getFragmentManager();
        mainFragment = mainFragment.newInstance();
        currentFragment = mainFragment;
        fm.beginTransaction()
                .replace(R.id.container, mainFragment)
                .commit();
        mBLEService = BLEService.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_SERVICE);
        this.registerReceiver(dataReceiver, filter);
    }

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mainFragment != null && currentFragment == mainFragment) {
                BluetoothGattCharacteristic accelCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("00000001-1212-efde-1523-785fef13d123"), BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ);
                BluetoothGattCharacteristic pressureCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("00000002-1212-efde-1523-785fef13d123"), BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ);
                BluetoothGattCharacteristic energyCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("00000003-1212-efde-1523-785fef13d123"), BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ);
                accelCharacteristic.setValue(intent.getByteArrayExtra("accel"));
                pressureCharacteristic.setValue(intent.getByteArrayExtra("pressure"));
                energyCharacteristic.setValue(intent.getByteArrayExtra("energy"));
                if (pressureCharacteristic.getValue() != null) {
                    mainFragment.updatePressureValue(pressureCharacteristic);
                }
                if (accelCharacteristic.getValue() != null) {
                    mainFragment.updateAccelValues(accelCharacteristic);
                }
                if (energyCharacteristic.getValue() != null) {
                    mainFragment.updateEnergyValue(energyCharacteristic);
                }
            }
        }
    };
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
        if (mBLEService.getmBluetoothAdapter() == null || !mBLEService.getmBluetoothAdapter().isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
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
        if (mainFragment != null && currentFragment == mainFragment)
            mainFragment.clearDisplayValues();
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
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        //Disconnect from any active tag connection
        if (mBLEService.getmGatt() != null) {
            Log.e("onStop", "disconnecting from " + mBLEService.getmGatt().getDevice().getName());
            //mConnectedGatt.disconnect();
//            mConnectedGatt = null;
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    protected void onDestroy() {

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


    private Handler mHandler = new Handler();

    private boolean scanning = false;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                if (scanning) {
                    item.setTitle("Scanning");
                    mBLEService.getmDevices().clear();
                    mBLEService.stopScan();
                    invalidateOptionsMenu();
                } else {
                    item.setTitle("Scan");
                }
                mBLEService.getmDevices().clear();
                mBLEService.startScan();
                scanning = !scanning;
                mHandler.postDelayed(mRefreshMenu, 1000);
                return true;
            default:
                //Obtain the discovered device to connect with
                mBLEService.setmDevice(mBLEService.getmDevices().get(item.getItemId()));
                Log.i(TAG, "Connecting to " + mBLEService.getmDevice().getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                if (mBLEService.getmGatt() == null) {
                    mBLEService.setmGatt(mBLEService.connectToDevice(mBLEService.getmDevice()));
                }
                mBLEService.stopScan();

                //Display progress UI
                return super.onOptionsItemSelected(item);
        }
    }

    private Runnable mRefreshMenu = new Runnable() {
        @Override
        public void run() {
            invalidateOptionsMenu();
        }
    };


    @Override
    public void onStart() {
        super.onStart();
    }
}
