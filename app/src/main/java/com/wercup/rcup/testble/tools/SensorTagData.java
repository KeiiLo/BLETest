package com.wercup.rcup.testble.tools;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import android.util.StringBuilderPrinter;

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
        coefficients[0] = shortSignedAtOffset(c, 6);
        Log.i("Accel", "Y");
        coefficients[1] = shortSignedAtOffset(c, 4);
        Log.i("Accel", "Z");
        coefficients[2] = shortSignedAtOffset(c, 2);
        Log.i("Accel", "Temp");
        coefficients[3] = getTemp(c);

        return coefficients;
    }

    public static double getBatteryLevel (BluetoothGattCharacteristic c) {
        int rawValue;
        double processedValue;

        rawValue = shortSignedAtOffsetBattery(c, 1);
        processedValue = (double) rawValue / 1000;
        return processedValue;
    }

    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature
     * all store 16 bit two's complement values in the awkward format
     * LSB MSB, which cannot be directly parsed as getIntValue(FORMAT_SINT16, offset)
     * because the bytes are stored in the "wrong" direction.
     *
     * This function extracts these 16 bit two's complement values.
     * */


    private static Integer shortSignedAtOffsetBattery(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);

        return (lowerByte << 8) + upperByte;
    }
    private static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
//        Log.i("Accel", "lowerByte " + lowerByte);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1); // Note: interpret MSB as signed.
//        Log.i("Accel", "upperByte " + upperByte);
        Integer test = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset);
//        Log.i("Accel", "Test " + test);
        Integer test2 = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);
//        Log.i("Accel", "Test2 " + test2);

//        Log.e("Accel", "Test java " + ((test << 8) + test2));
//        Log.w("Accel", "Test java " + ((lowerByte << 8) + upperByte));
        return (test << 8) + test2;
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

        return (upperByte << 8) + lowerByte;
    }

    private static Integer getTemp (BluetoothGattCharacteristic c) {
        byte[] rawData = c.getValue();
        byte[] tempData = new byte[2];
        System.arraycopy(rawData, 6, tempData, 0, 2);
        StringBuilder sb = new StringBuilder();
        for (byte b : rawData) {
            sb.append(String.format("%02X ", b));
        }
        StringBuilder sb2 = new StringBuilder();
        for (byte b : tempData) {
            sb2.append(String.format("%02X ", b));
        }
        Log.e("Temp raw value", String.valueOf(sb2));
        Log.e("Accel raw value", String.valueOf(sb));

        return c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
//        Log.i("Steps", "[1] " + lowerByte);
        Integer lower2Byte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
//        Log.i("Steps", "[2] " + lower2Byte);
        Integer lower3Byte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
//        Log.i("Steps", "[3] " + lower3Byte);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4); // Note: interpret MSB as unsigned.
//        Log.i("Steps", "[4] " + upperByte);
//        return upperByte;
        return (lowerByte << 24) + (lower2Byte << 16) + (lower3Byte << 8) + upperByte;
    }
}