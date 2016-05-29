package ru.lifeplus.pushshock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by pavel on 20.05.2016.
 */
public class ShockSettingsFragment extends Fragment implements MainActivity.OnMainActivityInteractionListener,
        SeekBar.OnSeekBarChangeListener, ShockDevice.DeviceStateListener {
    private final boolean DBG = false;

    /**
     * The {@link ShockDevice}
     */
    private ShockDevice mShockDevice;

    /**
     * The {@link View}
     */
    private View mRootView;

    public ShockSettingsFragment () {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_shock_settings, container, false);

        SeekBar dt = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_dt);
        dt.setMax(255);
        dt.setOnSeekBarChangeListener(this);
        SeekBar ads = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_ads);
        ads.setMax(255);
        ads.setOnSeekBarChangeListener(this);
        SeekBar nmpp = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_nmpp);
        nmpp.setMax(255);
        nmpp.setOnSeekBarChangeListener(this);
        SeekBar np = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_np);
        np.setMax(255);
        np.setOnSeekBarChangeListener(this);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShockDevice == null && !DBG) {
            hideDeviceInfo();
            hideDeviceSettings();
        } else {
            showDeviceInfo();
            showDeviceSettings();
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
                /**
                 * Show device info
                 */
                showDeviceInfo();

                /**
                 * Show device settings
                 */
                showDeviceSettings();
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

    void showDeviceSettings() {
        ShockDeviceSettings settings = mShockDevice.getSettings();
        if (settings != null) {
            if (mRootView != null) {
                SeekBar dt = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_dt);
                //dt.setMax(255);
                dt.setProgress((int) settings.dt);

                SeekBar ads = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_ads);
                //ads.setMax(255);
                ads.setProgress((int) settings.ads);

                SeekBar nmpp = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_nmpp);
                //nmpp.setMax(255);
                nmpp.setProgress((int) settings.nmpp);

                SeekBar np = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_np);
                //np.setMax(255);
                np.setProgress((int) settings.np);

                TextView stamp = (TextView) mRootView.findViewById(R.id.label_settings_stamp);
                stamp.setText(settings.stamp.toString());

                mRootView.findViewById(R.id.device_settings).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDeviceDeselected(ShockDevice shockDevice) {
        if (mShockDevice == shockDevice) {
            hideDeviceInfo();
            hideDeviceSettings();
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

    void hideDeviceSettings() {
        if (mRootView != null) {
            SeekBar dt = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_dt);
            dt.setProgress(0);

            SeekBar ads = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_ads);
            ads.setProgress(0);

            SeekBar nmpp = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_nmpp);
            nmpp.setProgress(0);

            SeekBar np = (SeekBar) mRootView.findViewById(R.id.seekbar_settings_np);
            np.setProgress(0);

            TextView stamp = (TextView) mRootView.findViewById(R.id.label_settings_stamp);
            stamp.setText("");

            mRootView.findViewById(R.id.device_settings).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //nothing
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mShockDevice != null && mShockDevice.getSettings() != null) {
            ShockDeviceSettings settings = mShockDevice.getSettings();
            switch (seekBar.getId()) {
                case R.id.seekbar_settings_dt:
                    if (DBG) {
                        Toast.makeText(getActivity(), getString(R.string.seekbar_settings_format, "seekbar_settings_dt", seekBar.getProgress()), Toast.LENGTH_SHORT).show();
                    }
                    settings.dt = (byte) seekBar.getProgress();
                    break;
                case R.id.seekbar_settings_ads:
                    if (DBG) {
                        Toast.makeText(getActivity(), getString(R.string.seekbar_settings_format, "seekbar_settings_ads", seekBar.getProgress()), Toast.LENGTH_SHORT).show();
                    }
                    settings.ads = (byte) seekBar.getProgress();
                    break;
                case R.id.seekbar_settings_nmpp:
                    if (DBG) {
                        Toast.makeText(getActivity(), getString(R.string.seekbar_settings_format, "seekbar_settings_nmpp", seekBar.getProgress()), Toast.LENGTH_SHORT).show();
                    }
                    settings.nmpp = (byte) seekBar.getProgress();
                    break;
                case R.id.seekbar_settings_np:
                    if (DBG) {
                        Toast.makeText(getActivity(), getString(R.string.seekbar_settings_format, "seekbar_settings_np", seekBar.getProgress()), Toast.LENGTH_SHORT).show();
                    }
                    settings.np = (byte) seekBar.getProgress();
                    break;
                default:
                    Toast.makeText(getActivity(), getString(R.string.seekbar_settings_format, "UNKNOWN SEEKBAR!", seekBar.getProgress()), Toast.LENGTH_LONG).show();
            }
            settings.stamp = new Date();
            mShockDevice.setSettings(settings);
        }
    }


    @Override
    public void onDeviceReady() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDeviceSettings();
                if (mRootView != null) {
                    mRootView.findViewById(R.id.device_info).setEnabled(true);
                    LinearLayout layout = (LinearLayout) mRootView.findViewById(R.id.device_settings);
                    for (int i = 0; i < layout.getChildCount(); i++) {
                        View child = layout.getChildAt(i);
                        child.setEnabled(true);
                    }
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
                    LinearLayout layout = (LinearLayout) mRootView.findViewById(R.id.device_settings);
                    for (int i = 0; i < layout.getChildCount(); i++) {
                        View child = layout.getChildAt(i);
                        child.setEnabled(false);
                    }
                }
            }
        });
    }

}