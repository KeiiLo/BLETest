package com.wercup.rcup.testble.fragments;

import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wercup.rcup.testble.BLEService.BLEService;
import com.wercup.rcup.testble.BLEService.BLESettings;
import com.wercup.rcup.testble.MainActivity;
import com.wercup.rcup.testble.R;
import com.wercup.rcup.testble.tools.SensorTagData;


public class mainFragment extends Fragment {

    public TextView mXAccel, mYAccel, mZAccel, mTapTap, mTemp, mStep, mBattery, mBattery2, mRefresh;
    public Button mReadConfig, mSendConfig;

    public MainActivity mainActivity;
    // On Alex's request.
    private int mHorodata;

    public mainFragment() {
    }

    public static mainFragment newInstance() {
        mainFragment fragment = new mainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainActivity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mXAccel = (TextView) view.findViewById(R.id.text_X);
        mYAccel = (TextView) view.findViewById(R.id.text_Y);
        mZAccel = (TextView) view.findViewById(R.id.text_Z);
        mTapTap = (TextView) view.findViewById(R.id.text_taptap);
        mTemp = (TextView) view.findViewById(R.id.text_temperature);
        mStep = (TextView) view.findViewById(R.id.text_step);
        mBattery = (TextView) view.findViewById(R.id.text_battery);
        mBattery2 = (TextView) view.findViewById(R.id.text_battery2);
        mRefresh = (TextView) view.findViewById(R.id.text_refresh_rate);
        mReadConfig = (Button) view.findViewById(R.id.btn_read);
        mReadConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("ButtonClick", "Clicking on read");
                /*BLEService.readEnergyConfig(mainActivity.getmBLEService().getmGatt());*/
                BLESettings.setResetCounter(1);
                BLEService.sendPressureConfig(mainActivity.getmBLEService().getmGatt());

            }
        });
        mSendConfig = (Button) view.findViewById(R.id.btn_config);
        mSendConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ButtonClick", "Clicking on Send");
                /*if (mainActivity.getmBLEService().getmGatt() != null) {
                    BLEService.sendEnergyConfig(mainActivity.getmBLEService().getmGatt());
                }*/
                FragmentManager fm = getFragmentManager();
                MainActivity.settingsFragment = settingsFragment.newInstance();
                MainActivity.currentFragment = MainActivity.settingsFragment;
                fm.beginTransaction()
                        .replace(R.id.container, MainActivity.settingsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }


    /* Methods to extract sensor data and update the UI */
    private int[] values;

    public void updateAccelValues(BluetoothGattCharacteristic characteristic) {
        values = SensorTagData.extractAccelCoefficients(characteristic);
        mXAccel.setText(String.valueOf(values[0]));
        mYAccel.setText(String.valueOf(values[1]));
        mZAccel.setText(String.valueOf(values[2]));
        mTemp.setText(String.valueOf(values[3]));
    }

    public void updateEnergyValue(BluetoothGattCharacteristic characteristic) {
        double[] batteryLevel = SensorTagData.getBatteryLevel(characteristic);
        mBattery.setText(String.valueOf(batteryLevel[0]) + "mA");
        mBattery2.setText(String.valueOf(batteryLevel[1]) + "V");
    }


    public void updatePressureValue(BluetoothGattCharacteristic characteristic) {
        if (SensorTagData.isTapTap(characteristic)) {
            mTapTap.setText("True");
        } else {
            mTapTap.setText("False");
        }
        int[] steps = SensorTagData.getSteps(characteristic);
        mHorodata = steps[1];
        mStep.setText(String.valueOf(steps[0]));
    }


    public void clearDisplayValues() {
        mXAccel.setText("---");
        mYAccel.setText("---");
        mZAccel.setText("---");
        mTapTap.setText("---");
        mTemp.setText("---");
        mStep.setText("---");
        mBattery.setText("---");
        mBattery2.setText("---");
        mRefresh.setText("---");
    }

    @Override
    public void onAttach(Context context) {
        Log.e("Main onAttach", "Here");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
