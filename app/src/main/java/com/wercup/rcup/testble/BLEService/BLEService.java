/*
package com.wercup.rcup.testble.BLEService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.wercup.rcup.testble.tools.SensorTagData;

import java.util.List;
import java.util.UUID;

*/
/**
 * Created by KeiLo on 27/09/16.
 *//*


public class BLEService implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "BluetoothGattActivity";
    private static final String DEVICE_NAME = "Smart Sole 001";

    private SparseArray<BluetoothDevice> mDevices;

    */
/* Accelerometer Service *//*

    private static final UUID ACCEL_SERVICE = UUID.fromString("00002400-1212-efde-1523-785fef13d123");
    private static final UUID ACCEL_DATA_CHAR = UUID.fromString("00002401-1212-efde-1523-785fef13d123");
    private static final UUID ACCEL_CONFIG_CHAR = UUID.fromString("00002402-1212-efde-1523-785fef13d123");
    */
/* Step Counter Service *//*

    private static final UUID PRESSURE_SERVICE = UUID.fromString("00002500-1212-efde-1523-785fef13d123");
    private static final UUID PRESSURE_DATA_CHAR = UUID.fromString("00002501-1212-efde-1523-785fef13d123");
    private static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("00002502-1212-efde-1523-785fef13d123");
    */
/* Client Configuration Descriptor *//*

    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



    */
/* BluetoothAdapter.LeScanCallback *//*


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getAddress() + " @ " + rssi);
        */
/*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         *//*

        if (DEVICE_NAME.equals(device.getName())) {
            mDevices.put(device.hashCode(), device);
            //Update the overflow menu
            invalidateOptionsMenu();
        }
    }

    */
/*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     *//*

    public static BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        */
/* State Machine Tracking *//*

        private int mState = 0;

        private void reset() {
            mState = 0;
        }

        private void advance() {
            Log.e("Advance", "Called ! mState is " + mState);
            mState++;
        }

        */
/*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         *//*

        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Enabling pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[]{0x02});
                    break;
                case 1:
                    Log.d(TAG, "Enabling pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[]{0x01});
                    break;
                case 2:
                    Log.d(TAG, "Enabling humidity");
                    characteristic = gatt.getService(ACCEL_SERVICE)
                            .getCharacteristic(ACCEL_CONFIG_CHAR);
                    characteristic.setValue(new byte[]{0x01});
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.writeCharacteristic(characteristic);
        }

        */
/*
         * Read the data characteristic's value for each sensor explicitly
         *//*

        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Reading piezo data");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Reading Accel Data");
                    characteristic = gatt.getService(ACCEL_SERVICE)
                            .getCharacteristic(ACCEL_DATA_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.readCharacteristic(characteristic);
        }

        */
/*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         *//*

        private void setNotifyNextSensor(BluetoothGatt gatt) {
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
                default:
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
                */
/*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 *//*

                gatt.discoverServices();
                Log.e("TEST", "Test");
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                */
/*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 *//*

                mHandler.sendEmptyMessage(MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                */
/*
                 * If there is a failure at any stage, simply disconnect
                 *//*

                gatt.disconnect();
                gatt.close();
            }
        }

        public void showServices(BluetoothGatt gatt) {
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services ) {
                Log.i("onServicesDiscovered", service.getUuid().toString());
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: " + status);
            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            */
/*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             *//*

            showServices(gatt);
            reset();
            //enableNextSensor(gatt);
            setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (ACCEL_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCEL, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }

            //After reading the initial value, next we enable notifications
            setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            Log.e("onCharacteristicWrite", "Here");
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            */
/*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             *//*

            if (ACCEL_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCEL, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            //enableNextSensor(gatt);
            setNotifyNextSensor(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: " + rssi);
        }

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
    };


}
*/
