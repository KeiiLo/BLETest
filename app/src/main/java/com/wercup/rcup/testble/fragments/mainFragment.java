package com.wercup.rcup.testble.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wercup.rcup.testble.R;
import com.google.android.gms.plus.PlusOneButton;


/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private final String TAG = "MainFragment";
    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;
    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";
    // TODO: Rename and change types of parameters
    private PlusOneButton mPlusOneButton;

    public TextView mXAccel, mYAccel, mZAccel, mTapTap, mTemp, mStep, mBattery, mBattery2, mRefresh;
    private Button mReadConfig, mSendConfig;
    private OnFragmentInteractionListener mListener;

    private static MainFragment mInstance;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public static MainFragment getInstance() {
        if (mInstance == null) {
            mInstance = newInstance();
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        /*
         * We are going to display the results in some text fields
         */
        //Find the +1 button
//        mPlusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
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
        mSendConfig = (Button) view.findViewById(R.id.btn_config);

        mReadConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SettingsFragment fragment = SettingsFragment.newInstance();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_main, fragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

    }


    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

}
