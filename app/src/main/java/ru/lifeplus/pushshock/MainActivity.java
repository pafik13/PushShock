package ru.lifeplus.pushshock;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnListFragmentInteractionListener {

    // TODO: Check if service better
    public interface OnMainActivityInteractionListener {
        /**
         * Call when device selected
         * @param shockDevice is device to select
         */
        void onDeviceSelected(ShockDevice shockDevice);

        /**
         * Call when device deselected
         * @param shockDevice is device to deselect
         */
        void onDeviceDeselected(ShockDevice shockDevice);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    List<WeakReference<Fragment>> mFragmentRefList;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * The {@link BluetoothGattCallback}
     */
    public BluetoothGattCallback mBluetoothGattCallback;

    /**
     * The {@link ShockDevice}
     */
    private ShockDevice mShockDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mFragmentRefList = new ArrayList<>();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(MainActivity.this, "dsadsa", Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
            fab.setVisibility(View.GONE);
        }

        //
        mBluetoothGattCallback = new BluetoothGattCallback() {
            public final String TAG = "BluetoothGattCallback";
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    Log.i(TAG, "Call <discoverServices()>; " + ts);
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (mShockDevice.mBluetoothGatt.equals(gatt))
                    {
                        mShockDevice.deselect();
                        mShockDevice = null;
                    }
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //gatt.getServices()
                    //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    Log.w(TAG, "onServicesDiscovered received: " + "GATT_SUCCESS");
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
        };
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        mFragmentRefList.add(new WeakReference(fragment));
        if (mShockDevice != null) {
            if (fragment instanceof OnMainActivityInteractionListener) {
                ((OnMainActivityInteractionListener) fragment).onDeviceSelected(mShockDevice);
            }
            if (fragment instanceof ShockDevice.DeviceStateListener) {
                mShockDevice.addDeviceStateListener((ShockDevice.DeviceStateListener) fragment);
            }
        }
    }

    private List<Fragment> getFragments() {
        ArrayList<Fragment> result = new ArrayList<>();
        for(WeakReference<Fragment> ref : mFragmentRefList) {
            Fragment fragment = ref.get();
            if(fragment != null) {
                result.add(fragment);
            }
        }
        return result;
    }

    @Override
    public void onListFragmentInteraction(BluetoothDevice item) {
        // Here device after create immediately connecting
        ShockDevice shockDevice = new ShockDevice(this, item);
        if (mShockDevice == null) {
            SelectShockDevice(shockDevice);
        } else {
            if (item.getAddress().compareTo(mShockDevice.mBluetoothDevice.getAddress()) == 0) {
                DeselectShockDevice();
                Toast.makeText(this, getString(R.string.device_deselected, mShockDevice.mBluetoothDevice.getAddress())
                        , Toast.LENGTH_SHORT).show();
                return;
            } else {
                DeselectShockDevice();
                SelectShockDevice(shockDevice);
            }
        }
        Toast.makeText(this, getString(R.string.device_selected, mShockDevice.mBluetoothDevice.getAddress())
                , Toast.LENGTH_SHORT).show();
    }

    void SelectShockDevice(ShockDevice shockDevice){
        if (shockDevice.select()){
            for (Fragment fragment : getFragments()) {
                if (fragment instanceof OnMainActivityInteractionListener) {
                    ((OnMainActivityInteractionListener) fragment).onDeviceSelected(shockDevice);
                }
            }
            mShockDevice = shockDevice;
        } else {
            Toast.makeText(this, R.string.device_not_selected, Toast.LENGTH_SHORT).show();
        }
    }

    void DeselectShockDevice(){
        mShockDevice.deselect();
        for (Fragment fragment : getFragments()) {
            if (fragment instanceof OnMainActivityInteractionListener) {
                ((OnMainActivityInteractionListener) fragment).onDeviceDeselected(mShockDevice);
            }
        }
        mShockDevice = null;
//        } else {
//            Toast.makeText(this, R.string.device_not_deselected, Toast.LENGTH_SHORT).show();
//        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new DeviceListFragment();
                case 1:
                    return new ShockButtonFragment();
                case 2:
                    return new ShockSettingsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "УСТРОЙСТВА";
                case 1:
                    return "РАЗРЯД";
                case 2:
                    return "НАСТРОЙКИ";
            }
            return null;
        }
    }
}
