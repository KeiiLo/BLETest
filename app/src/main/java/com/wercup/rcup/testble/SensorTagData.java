package com.wercup.rcup.testble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by KeiLo on 22/09/16.
 */

public class SensorTagData {

    public static boolean isTapTap(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getValue()[0] == 1);
    }

    public static int getSteps(BluetoothGattCharacteristic c) {
        return shortUnsignedAtOffset(c);
    }

    public static int[] extractAccelCoefficients(BluetoothGattCharacteristic c) {
        int[] coefficients = new int[4];

        Log.i("Accel", "X");
        coefficients[0] = shortSignedAtOffset(c, 0);
        Log.i("Accel", "Y");
        coefficients[1] = shortSignedAtOffset(c, 2);
        Log.i("Accel", "Z");
        coefficients[2] = shortSignedAtOffset(c, 4);
        Log.i("Accel", "Temp");
        coefficients[3] = shortSignedAtOffset(c, 6);

        return coefficients;
    }

    public static double extractBarTemperature(BluetoothGattCharacteristic characteristic, final int[] c) {
        // c holds the calibration coefficients

        int t_r;	// Temperature raw value from sensor
        double t_a; 	// Temperature actual value in unit centi degrees celsius

        t_r = shortSignedAtOffset(characteristic, 0);

        t_a = (100 * (c[0] * t_r / Math.pow(2,8) + c[1] * Math.pow(2,6))) / Math.pow(2,16);

        return t_a / 100;
    }

    public static double extractBarometer(BluetoothGattCharacteristic characteristic, final int[] c) {
        // c holds the calibration coefficients

        int t_r;	// Temperature raw value from sensor
        int p_r;	// Pressure raw value from sensor
        double S;	// Interim value in calculation
        double O;	// Interim value in calculation
        double p_a; 	// Pressure actual value in unit Pascal.

        t_r = shortSignedAtOffset(characteristic, 0);
        p_r = shortUnsignedAtOffset(characteristic, 2);


        S = c[2] + c[3] * t_r / Math.pow(2,17) + ((c[4] * t_r / Math.pow(2,15)) * t_r) / Math.pow(2,19);
        O = c[5] * Math.pow(2,14) + c[6] * t_r / Math.pow(2,3) + ((c[7] * t_r / Math.pow(2,15)) * t_r) / Math.pow(2,4);
        p_a = (S * p_r + O) / Math.pow(2,14);

        //Convert pascal to in. Hg
        double p_hg = p_a * 0.000296;

        return p_hg;
    }

    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature
     * all store 16 bit two's complement values in the awkward format
     * LSB MSB, which cannot be directly parsed as getIntValue(FORMAT_SINT16, offset)
     * because the bytes are stored in the "wrong" direction.
     *
     * This function extracts these 16 bit two's complement values.
     * */
    private static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Log.i("Accel", "lowerByte " + lowerByte);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1); // Note: interpret MSB as signed.
        Log.i("Accel", "upperByte " + upperByte);
        Integer test = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset);
        Log.i("Accel", "Test " + test);
        Integer test2 = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);
        Log.i("Accel", "Test2 " + test2);

        Log.e("Accel", "Test java " + ((test << 8) + test2));
        return (upperByte << 8) + lowerByte;
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

        return (upperByte << 8) + lowerByte;
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
//        Log.i("Steps", "[1] " + lowerByte);
        Integer lower2Byte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
//        Log.i("Steps", "[2] " + lower2Byte);
        Integer lower3Byte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
        Log.i("Steps", "[3] " + lower3Byte);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4); // Note: interpret MSB as unsigned.
        Log.i("Steps", "[4] " + upperByte);
//        return upperByte;
        return (lowerByte << 24) + (lower2Byte << 16) + (lower3Byte << 8) + upperByte;
    }
}