package com.wercup.rcup.testble.tools;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import android.util.StringBuilderPrinter;

/**
 * Created by KeiLo on 22/09/16.
 */

public class SensorTagData {

    /**
     * Function that tells if a TapTap has been sent
     * @param characteristic BluetoothGattCharacteristic that is being read
     * @return true of false depending if there is a TapTap or not
     */
    public static boolean isTapTap(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getValue()[0] == 1);
    }

    /**
     * Function that returns the number of steps read from the BLE notification
     * @param c BluetoothGattCharacteristic that is being read
     * @return Step count as an integer
     */
    public static int getSteps(BluetoothGattCharacteristic c) {
        return shortUnsignedAtOffset(c);
    }

    /**
     * Get accelerometer XYZ-axis values and ambient temperature
     * @param c BluetoothGattCharacteristic that is being read
     * @return integer array containing the 4 useful values (Tested and OK)
     */
    public static int[] extractAccelCoefficients(BluetoothGattCharacteristic c) {
        int[] coefficients = new int[4];

        coefficients[0] = shortSignedAtOffset(c, 0);
        coefficients[1] = shortSignedAtOffset(c, 2);
        coefficients[2] = shortSignedAtOffset(c, 4);
        coefficients[3] = getTemp(c);

        return coefficients;
    }

    /**
     * Process battery related values
     * @param c BluetoothGattCharacteristic that is being read
     * @return integer array containing Current and Voltage
     */
    public static double[] getBatteryLevel (BluetoothGattCharacteristic c) {
        Integer[] rawValues = shortSignedAtOffsetBattery(c);
        double[] processedValues = new double[2];
//        Log.e("Battery", String.valueOf(rawValues[0]));
        processedValues[0] = (double) rawValues[0];
        processedValues[1] = (double) rawValues[1] / 1000;
        return processedValues;
    }

    /**
     * Custom function that converts raw notification data to an integer array
     * @param c BluetoothGattCharacteristic that is being read
     * @return raw battery related values in an array
     */
    private static Integer[] shortSignedAtOffsetBattery(BluetoothGattCharacteristic c) {
        Integer[] batteryValues = new Integer[2];
        Integer intensity = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
        batteryValues[0] = intensity;
        batteryValues[1] = (lowerByte << 8) + upperByte;
        return batteryValues;
    }

    /**
     * Get Two's complement from data
     * @param c BluetoothGattCharacteristic that is being read
     * @param offset offset by byte
     * @return Signed integer value
     */
    private static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);

        return (lowerByte << 8) + upperByte;
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

        return (upperByte << 8) + lowerByte;
    }

    /**
     * This function compute the ambient temperature following the instructions given in the communication protocol 1.0.2
     * @param c BluetoothGattCharacteristic that is being read
     * @return the temperature as an signed integer (can be a negative value, tests proved it)
     */
    private static Integer getTemp (BluetoothGattCharacteristic c) {
        byte[] rawData = c.getValue();
        byte[] tempData = new byte[2];
        int processedTemp;
        System.arraycopy(rawData, 6, tempData, 0, 2);
        processedTemp = (((tempData[0] << 8) + tempData[1]) >> 5) / 8 + 25;
//        Log.e("Temp Processed value", String.valueOf(processedTemp));

        return  processedTemp;
    }

    /**
     * This function is custom made for counting steps which are sent to us in the form of an array of 4 bytes
     * @param c BluetoothGattCharacteristic that is being read
     * @return Step value in unsigned int
     */
    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        Integer lower2Byte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
        Integer lower3Byte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4); // Note: interpret MSB as unsigned.
        return (lowerByte << 24) + (lower2Byte << 16) + (lower3Byte << 8) + upperByte;
    }


}