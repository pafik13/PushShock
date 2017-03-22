package ru.lifeplus.pushshock;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by pavel on 20.05.2016.
 */
public class ShockButtonFragment extends Fragment implements MainActivity.OnMainActivityInteractionListener, ShockDevice.DeviceStateListener, ShockDevice.DoorStateListener {
    ShockButtonFragment Me;
    /**
     * The {@link ShockDevice}
     */
    private ShockDevice mShockDevice;

    /**
     * The {@link View}
     */
    private View mRootView;

    public ShockButtonFragment () {
        Me = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_shock_button, container, false);
        Button button = (Button) mRootView.findViewById(R.id.button_shock);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mShockDevice != null) {
                    mShockDevice.shock();
                } else {
                    Toast.makeText(getActivity(), R.string.device_not_connected, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShockDevice == null) {
            hideDeviceInfo();
        } else {
            showDeviceInfo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDeviceSelected(ShockDevice shockDevice) {
        mShockDevice = shockDevice;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDeviceInfo();
                mShockDevice.addDoorStateListener(Me);
            }
        });
    }

    void showDeviceInfo(){
        if (mRootView != null) {
            TextView shockDeviceName = (TextView) mRootView.findViewById(R.id.device_name);
            shockDeviceName.setText(mShockDevice.mBluetoothDevice.getName());
            TextView shockDeviceAddress = (TextView) mRootView.findViewById(R.id.device_address);
            shockDeviceAddress.setText(mShockDevice.mBluetoothDevice.getAddress());

            mRootView.findViewById(R.id.device_info).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeviceDeselected(ShockDevice shockDevice) {
        if (mShockDevice == shockDevice) {
            hideDeviceInfo();
            mShockDevice = null;
        }
    }

    void hideDeviceInfo(){
        if (mRootView != null) {
            TextView shockDeviceName = (TextView) mRootView.findViewById(R.id.device_name);
            shockDeviceName.setText(R.string.no_name);
            TextView shockDeviceAddress = (TextView) mRootView.findViewById(R.id.device_address);
            shockDeviceAddress.setText(R.string.no_address);

            mRootView.findViewById(R.id.device_info).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDeviceReady() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRootView != null) {
                    mRootView.findViewById(R.id.device_info).setEnabled(true);
                    mRootView.findViewById(R.id.button_shock).setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onDeviceBusy() {
        //TODO: ADD SPINNER
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRootView != null) {
                    mRootView.findViewById(R.id.device_info).setEnabled(false);
                    mRootView.findViewById(R.id.button_shock).setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onDoorOpened() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRootView != null) {
                    CheckedTextView doorState = (CheckedTextView)mRootView.findViewById(R.id.door_state);
                    doorState.setText("ОТКРЫТА");
                    mRootView.setBackgroundColor(Color.GREEN);
                }
            }
        });
    }

    @Override
    public void onDoorClosed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRootView != null) {
                    CheckedTextView doorState = (CheckedTextView)mRootView.findViewById(R.id.door_state);
                    doorState.setText("ЗАКРЫТА");
                    mRootView.setBackgroundColor(Color.RED);
                }
            }
        });
    }
}
