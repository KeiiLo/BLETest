package com.wercup.rcup.testble.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.wercup.rcup.testble.BLEService.BLEService;
import com.wercup.rcup.testble.BLEService.BLESettings;
import com.wercup.rcup.testble.MainActivity;
import com.wercup.rcup.testble.R;

/**
 * Created by KeiLo on 18/11/16.
 */

public class settingsFragment extends Fragment {

    private static SeekBar energyRefresh, accelRefresh, outputRate, fullScale, compThres;
    private static TextView energyRefreshValue, accelRefreshValue, outputRateValue,
            fullScaleValue, compThresValue, compInputValue, compStateValue,
            crossValue, upValue, downValue, readyValue;
    private static Switch hystSwitch, zSwitch, ySwitch, xSwitch, bandwidthSwitch;

    private static Button setBtn;

    private static MainActivity mainActivity;

    public settingsFragment() {
    }

    public static settingsFragment newInstance() {
        settingsFragment fragment = new settingsFragment();
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
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initVariables(view);
        initValues();
        setListeners();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initValues() {

        // Seekbar values
        int value = BLESettings.getEnergyRefreshRate() / 1000;
        Log.e("Init settings view", String.valueOf(value));
        energyRefresh.setProgress(value - 1);
        energyRefreshValue.setText(String.format("%d s", value));
        value = BLESettings.getAccelRefreshRate() / 1000;
        accelRefresh.setProgress(value - 1);
        accelRefreshValue.setText(String.format("%d s", value));
        value = BLESettings.getAccelOutputRate();
        outputRate.setProgress(value);
        outputRateValue.setText(outputRate(value));
        value = BLESettings.getFullScaleSelection();
        fullScale.setProgress(value);
        fullScaleValue.setText(fullScale(value));
        value = BLESettings.getComparatorThres();
        compThres.setProgress(value);
        compThresValue.setText(compThreshold(value));

        // Read-only values
        value = BLESettings.getLCOMPInput();
        compInputValue.setText(input(value));
        value = BLESettings.getLCOMPState();
        compStateValue.setText(enabled(value));
        value = BLESettings.getUpEvent();
        upValue.setText(toBoolStr(value));
        value = BLESettings.getDownEvent();
        downValue.setText(toBoolStr(value));
        value = BLESettings.getCrossEvent();
        crossValue.setText(toBoolStr(value));
        value = BLESettings.getReadyEvent();
        readyValue.setText(toBoolStr(value));

        // Switch values
        value = BLESettings.getzAxis();
        zSwitch.setChecked(value == 1);
        value = BLESettings.getyAxis();
        ySwitch.setChecked(value == 1);
        value = BLESettings.getxAxis();
        xSwitch.setChecked(value == 1);
        value = BLESettings.getBandwidth();
        bandwidthSwitch.setChecked(value == 1);
        value = BLESettings.getEnableHyst();
        hystSwitch.setChecked(value == 1);
    }

    private void setListeners() {
        energyRefresh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress;
                energyRefreshValue.setText(String.format("%d s", (prog + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                energyRefreshValue.setText(String.format("%d s", (prog + 1)));
                BLESettings.setEnergyRefreshRate((prog + 1) * 1000);
            }
        });
        accelRefresh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress;
                accelRefreshValue.setText(String.format("%d s", (prog + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                accelRefreshValue.setText(String.format("%d s", (prog + 1)));
                BLESettings.setAccelRefreshRate((prog + 1) * 1000);
            }
        });
        outputRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress;
                outputRateValue.setText(outputRate(prog));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                outputRateValue.setText(outputRate(prog));
                BLESettings.setAccelOutputRate(prog);
            }
        });
        fullScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress != 1)
                    prog = progress;
                fullScaleValue.setText(fullScale(prog));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fullScaleValue.setText(fullScale(prog));
                BLESettings.setFullScaleSelection(prog);
            }
        });
        compThres.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress;
                compThresValue.setText(compThreshold(prog));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                compThresValue.setText(compThreshold(prog));
                BLESettings.setComparatorThres(prog);
            }
        });
        hystSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BLESettings.setEnableHyst((BLESettings.getEnableHyst() == 1) ? 1 : 0);
            }
        });
        zSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BLESettings.setzAxis((BLESettings.getzAxis() == 1) ? 1 : 0);
            }
        });
        ySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BLESettings.setyAxis((BLESettings.getyAxis() == 1) ? 1 : 0);
            }
        });
        xSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BLESettings.setxAxis((BLESettings.getxAxis() == 1) ? 1 : 0);
            }
        });
        bandwidthSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BLESettings.setBandwidth((BLESettings.getBandwidth() == 1) ? 1 : 0);
            }
        });

        setBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEService.sendEnergyConfig(mainActivity.getmBLEService().getmGatt());
            }
        });
    }

    private String outputRate(int value) {
        String string = null;
        switch (value) {
            case 0:
                string = "Power-down";
                break;
            case 1:
                string = "10Hz";
                break;
            case 2:
                string = "50Hz";
                break;
            case 3:
                string = "100Hz";
                break;
            case 4:
                string = "200Hz";
                break;
            case 5:
                string = "400Hz";
                break;
            case 6:
                string = "800Hz";
                break;
        }
        return string;
    }

    private String fullScale(int value) {
        String string = null;
        switch (value) {
            case 0:
                string = "±2g";
                break;
            case 2:
                string = "±4g";
                break;
            case 3:
                string = "±8g";
                break;
        }
        return string;
    }

    private String compThreshold(int value) {
        String string = null;
        switch (value) {
            case 0:
                string = "312mV";
                break;
            case 1:
                string = "625mV";
                break;
            case 2:
                string = "937mV";
                break;
            case 3:
                string = "1.27V";
                break;
            case 4:
                string = "1.56V";
                break;
            case 5:
                string = "1.875V";
                break;
            case 6:
                string = "2.18V";
                break;
        }
        return string;
    }

    private String input(int value) {
        if (value == 3) {
            return "OK";
        }
        else {
            return "Error";
        }
    }

    private String enabled(int value) {
        if (value == 1) {
            return "Enabled";
        }
        else {
            return "Disabled";
        }
    }

    private String toBoolStr(int value) {
        if (value == 1) {
            return "True";
        }
        else {
            return "False";
        }
    }

    private void initVariables(View v) {
        energyRefresh = (SeekBar) v.findViewById(R.id.energyrefresh_seek);
        accelRefresh = (SeekBar) v.findViewById(R.id.accelrefresh_seek);
        outputRate = (SeekBar) v.findViewById(R.id.output_seek);
        compThres = (SeekBar) v.findViewById(R.id.compthres_seek);
        fullScale = (SeekBar) v.findViewById(R.id.fullscale_seek);

        energyRefreshValue = (TextView) v.findViewById(R.id.energyrefresh_value);
        accelRefreshValue = (TextView) v.findViewById(R.id.accelrefresh_value);
        outputRateValue = (TextView) v.findViewById(R.id.output_value);
        fullScaleValue = (TextView) v.findViewById(R.id.fullscale_value);
        compThresValue = (TextView) v.findViewById(R.id.compthres_value);
        compInputValue = (TextView) v.findViewById(R.id.input_value);
        compStateValue = (TextView) v.findViewById(R.id.state_value);
        crossValue = (TextView) v.findViewById(R.id.cross_value);
        upValue = (TextView) v.findViewById(R.id.up_value);
        downValue = (TextView) v.findViewById(R.id.down_value);
        readyValue = (TextView) v.findViewById(R.id.ready_value);

        hystSwitch = (Switch) v.findViewById(R.id.hyst_switch);
        zSwitch = (Switch) v.findViewById(R.id.z_switch);
        ySwitch = (Switch) v.findViewById(R.id.y_switch);
        xSwitch = (Switch) v.findViewById(R.id.x_switch);
        bandwidthSwitch = (Switch) v.findViewById(R.id.bandwidth_switch);

        setBtn = (Button) v.findViewById(R.id.sendconfig_button);
    }

    @Override
    public void onAttach(Context context) {

        Log.e("Settings onAttach", "Here");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
