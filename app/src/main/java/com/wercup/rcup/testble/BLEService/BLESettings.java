package com.wercup.rcup.testble.BLEService;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.wercup.rcup.testble.tools.SensorTagData;

/**
 * Created by KeiLo on 17/11/16.
 */

public class BLESettings {

    private static int energyRefreshRate;

    private static int accelRefreshRate;
    private static int accelOutputRate;
    private static int zAxis;
    private static int yAxis;
    private static int xAxis;
    private static int fullScaleSelection;
    private static int bandwidth;

    private static int enableHyst;
    private static int comparatorThres;
    private static int LCOMPInput;
    private static int LCOMPState;
    private static int crossEvent;
    private static int upEvent;
    private static int downEvent;
    private static int readyEvent;

    private static int resetCounter;

    public static int getResetCounter() {
        return resetCounter;
    }

    public static void setResetCounter(int resetCounter) {
        BLESettings.resetCounter = resetCounter;
    }

    public static int getEnergyRefreshRate() {
        return energyRefreshRate;
    }

    public static void setEnergyRefreshRate(int energyRefreshRate) {
        BLESettings.energyRefreshRate = energyRefreshRate;
    }

    public static int getAccelRefreshRate() {
        return accelRefreshRate;
    }

    public static void setAccelRefreshRate(int accelRefreshRate) {
        BLESettings.accelRefreshRate = accelRefreshRate;
    }

    public static int getAccelOutputRate() {
        return accelOutputRate;
    }

    public static void setAccelOutputRate(int accelOutputRate) {
        BLESettings.accelOutputRate = accelOutputRate;
    }

    public static int getzAxis() {
        return zAxis;
    }

    public static void setzAxis(int zAxis) {
        BLESettings.zAxis = zAxis;
    }

    public static int getyAxis() {
        return yAxis;
    }

    public static void setyAxis(int yAxis) {
        BLESettings.yAxis = yAxis;
    }

    public static int getxAxis() {
        return xAxis;
    }

    public static void setxAxis(int xAxis) {
        BLESettings.xAxis = xAxis;
    }

    public static int getFullScaleSelection() {
        return fullScaleSelection;
    }

    public static void setFullScaleSelection(int fullScaleSelection) {
        BLESettings.fullScaleSelection = fullScaleSelection;
    }

    public static int getBandwidth() {
        return bandwidth;
    }

    public static void setBandwidth(int bandwidth) {
        BLESettings.bandwidth = bandwidth;
    }

    public static int getEnableHyst() {
        return enableHyst;
    }

    public static void setEnableHyst(int enableHyst) {
        BLESettings.enableHyst = enableHyst;
    }

    public static int getComparatorThres() {
        return comparatorThres;
    }

    public static void setComparatorThres(int comparatorThres) {
        BLESettings.comparatorThres = comparatorThres;
    }

    public static int getLCOMPInput() {
        return LCOMPInput;
    }

    public static void setLCOMPInput(int LCOMPInput) {
        BLESettings.LCOMPInput = LCOMPInput;
    }

    public static int getLCOMPState() {
        return LCOMPState;
    }

    public static void setLCOMPState(int LCOMPState) {
        BLESettings.LCOMPState = LCOMPState;
    }

    public static int getCrossEvent() {
        return crossEvent;
    }

    public static void setCrossEvent(int crossEvent) {
        BLESettings.crossEvent = crossEvent;
    }

    public static int getUpEvent() {
        return upEvent;
    }

    public static void setUpEvent(int upEvent) {
        BLESettings.upEvent = upEvent;
    }

    public static int getDownEvent() {
        return downEvent;
    }

    public static void setDownEvent(int downEvent) {
        BLESettings.downEvent = downEvent;
    }

    public static int getReadyEvent() {
        return readyEvent;
    }

    public static void setReadyEvent(int readyEvent) {
        BLESettings.readyEvent = readyEvent;
    }


    public static void parsePressureConfig(BluetoothGattCharacteristic c) {
        byte[] trame = c.getValue();
        Log.e("LOL", SensorTagData.bytesToHex(trame));
        Log.e("LOL", SensorTagData.byteToHex(trame[1]));
        int hyst = trame[2] & 1;
        int LCOMPInput = trame[2] >> 5;
        int CompThres = ((trame[2] - (LCOMPInput << 5)) >>> 1);
        int LCOMPState = trame[1] & 1;
        int cross = (trame[1] >> 1) & 1;
        int up = (trame[1] >> 2) & 1;
        int down = (trame[1] >> 3) & 1;
        int ready = (trame[1] >> 4) & 1;
        setEnableHyst(hyst);
        setLCOMPInput(LCOMPInput);
        setComparatorThres(CompThres);
        setLCOMPState(LCOMPState);
        setCrossEvent(cross);
        setUpEvent(up);
        setDownEvent(down);
        setReadyEvent(ready);
    }

    public static void parseEnergyConfig(BluetoothGattCharacteristic c) {
        byte[] trame = c.getValue();
        int energyRefresh = SensorTagData.shortUnsignedAtOffset(c, 1);
        setEnergyRefreshRate(energyRefresh);
        Log.e("Parse Energy Config", "value " + energyRefresh);
    }

    public static void parseAccelConfig(BluetoothGattCharacteristic c) {
        byte[] trame = c.getValue();
        int accelRefresh = SensorTagData.shortUnsignedAtOffset(c, 3);

        int padding = (trame[2] >> 3);
        int outputRate= (trame[2] - (padding << 3));
        int z = (trame[2] >> 3) & 1;
        int y = (trame[2] >> 4) & 1;
        int x = (trame[2] >> 5) & 1;
        int fullScale = (trame[1] << 6) >>> 6;
        int bandwidth = (trame[1] << 6) & 1;
        /*Log.e("Parse Accel Config", "value " + accelRefresh);
        Log.e("Parse Accel Config", "value " + outputRate);
        Log.e("Parse Accel Config", "value " + z);
        Log.e("Parse Accel Config", "value " + y);
        Log.e("Parse Accel Config", "value " + x);
        Log.e("Parse Accel Config", "value " + fullScale);
        Log.e("Parse Accel Config", "value " + bandwidth);*/
        setAccelRefreshRate(accelRefresh);
        setAccelOutputRate(outputRate);
        setzAxis(z);
        setyAxis(y);
        setxAxis(x);
        setFullScaleSelection(fullScale);
        setBandwidth(bandwidth);
    }

}
