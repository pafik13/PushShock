package ru.lifeplus.pushshock;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by pavel on 19.05.2016.
 */
public final class ShockDevice {
    public final String TAG = "ShockDevice";
    //public static final UUID SHOCK_DEVICE_UUID = "ru.myapp.shockdevice.SHOCK_DEVICE_UUID";

    public static final String PUSH_SERVICE_ADDRESS        = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final UUID PUSH_SERVICE 		           = UUID.fromString(PUSH_SERVICE_ADDRESS);
    public static final String PUSH_CHARACTERISTIC_ADDRESS = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static final UUID PUSH_CHARACTERISTIC           = UUID.fromString(PUSH_CHARACTERISTIC_ADDRESS);
    public static final String SETTINGS_ADDRESS 		   = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static final UUID SETTINGS 			    	   = UUID.fromString(SETTINGS_ADDRESS);

    public enum DeviceState {
        NOT_READY, SERVICES_DISCOVERED, READY, BUSY
    }

    public interface DeviceStateListener {
        void onDeviceReady();
        void onDeviceBusy();
    }

    List<WeakReference<DeviceStateListener>> mDeviceStateListeners = new ArrayList<>();

    //TODO: do something with warning
    public void addDeviceStateListener(DeviceStateListener listener) {
        WeakReference<DeviceStateListener> newItem = new WeakReference(listener);
        mDeviceStateListeners.add(newItem);
    }

    Context mContext;

    BluetoothDevice mBluetoothDevice;

    ShockDeviceSettings mShockDeviceSettings;

    BluetoothGattCallback mBluetoothGattCallback;

    BluetoothGatt mBluetoothGatt;

    BluetoothGattService mGattPushService;

    DeviceState mDeviceState = DeviceState.NOT_READY;

    public ShockDevice(Context context, BluetoothDevice bluetoothDevice) {
        createCallBack();
        // TODO: Check UUID
        mContext = context;
        mBluetoothDevice = bluetoothDevice;
        connect();
    }

    public void createCallBack() {
        mBluetoothGattCallback = new BluetoothGattCallback() {
            public final String TAG = "BluetoothGattCallback";

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    Log.i(TAG, "Call <discoverServices()>; " + ((Long) System.currentTimeMillis()).toString());
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (mBluetoothGatt.equals(gatt))
                    {
                        disconnect();
                    }
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    mBluetoothGatt = gatt;
                    setDeviceState(DeviceState.SERVICES_DISCOVERED);
                    Log.w(TAG, "onServicesDiscovered received: " + "GATT_SUCCESS");
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            // Something was read
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.w(TAG, "onCharacteristicRead received: " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (characteristic.getUuid().compareTo(SETTINGS) == 0) {
                        byte[] values = characteristic.getValue();
                        if (values != null) {
                            mShockDeviceSettings = new ShockDeviceSettings(values);
                        }
                    }
                }
                setDeviceState(DeviceState.READY);
            }

            @Override
            // Something was write
            public void  onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.w(TAG, "onCharacteristicWrite received: " + status);
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                }
                setDeviceState(DeviceState.READY);
            }

        };
    }

    public void setDeviceState(DeviceState newState)
    {
        switch(newState) {
            case NOT_READY:
                mDeviceState = DeviceState.NOT_READY;
                break;
            case SERVICES_DISCOVERED:
                mDeviceState = DeviceState.SERVICES_DISCOVERED;
                readSettings();
                break;
            case READY:
                mDeviceState = DeviceState.READY;
                //for all mDeviceStateListeners { onDeviceReady()}
                for(WeakReference<DeviceStateListener> ref : mDeviceStateListeners) {
                    DeviceStateListener listener = ref.get();
                    if(listener != null) {
                        listener.onDeviceReady();
                    } else {
                        mDeviceStateListeners.remove(ref);
                    }
                }
                break;
            case BUSY:
                mDeviceState = DeviceState.BUSY;
                //for all listeners call onDeviceBUSY();
                for(WeakReference<DeviceStateListener> ref : mDeviceStateListeners) {
                    DeviceStateListener listener = ref.get();
                    if(listener != null) {
                        listener.onDeviceBusy();
                    } else {
                        mDeviceStateListeners.remove(ref);
                    }
                }
                break;
        }
    }

    private void readSettings(){
        if (mDeviceState == DeviceState.SERVICES_DISCOVERED || mDeviceState == DeviceState.READY){
            setDeviceState(DeviceState.BUSY);
            if (mGattPushService == null) {
                mGattPushService = mBluetoothGatt.getService(PUSH_SERVICE);
            }
            if (mGattPushService != null) {
                BluetoothGattCharacteristic characteristic = mGattPushService.getCharacteristic(SETTINGS);
                if (characteristic != null) {
                    mBluetoothGatt.readCharacteristic(characteristic);
                }
            }
            setDeviceState(DeviceState.READY);
        }
    }

    private void writeSettings(ShockDeviceSettings shockDeviceSettings){
        if (mDeviceState == DeviceState.SERVICES_DISCOVERED || mDeviceState == DeviceState.READY){
            setDeviceState(DeviceState.BUSY);
            if (mGattPushService == null) {
                mGattPushService = mBluetoothGatt.getService(PUSH_SERVICE);
            }
            if (mGattPushService != null) {
                BluetoothGattCharacteristic characteristic = mGattPushService.getCharacteristic(SETTINGS);
                if (characteristic != null) {
                    characteristic.setValue(shockDeviceSettings.toArray());
                    mBluetoothGatt.writeCharacteristic(characteristic);
                }
            }
            setDeviceState(DeviceState.READY);
        }
    }

    public boolean shock(){
        if (mDeviceState == DeviceState.SERVICES_DISCOVERED || mDeviceState == DeviceState.READY){
            setDeviceState(DeviceState.BUSY);
            byte[] value = {1};
            if (mGattPushService == null) {
                mGattPushService = mBluetoothGatt.getService(PUSH_SERVICE);
            }
            BluetoothGattCharacteristic characteristic = mGattPushService.getCharacteristic(PUSH_CHARACTERISTIC);
            characteristic.setValue(value);
            mBluetoothGatt.writeCharacteristic(characteristic);
            return true;
        }
        return false;
    }

    public ShockDeviceSettings getSettings() {
        return mShockDeviceSettings;
    }

    public boolean setSettings(ShockDeviceSettings shockDeviceSettings) {
       writeSettings(shockDeviceSettings);
        mShockDeviceSettings = shockDeviceSettings;
        return true;
    }

    public boolean connect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.connect();
        }
        if (mBluetoothGatt == null) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCallback);
        }

        Log.i(TAG, "Call <connect()>; " + ((Long) System.currentTimeMillis()).toString());
        return (mBluetoothGatt != null);
    }

    public boolean select(){
        return connect();
    }

    public void disconnect(){
        mShockDeviceSettings = null;
        mGattPushService = null;
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void deselect() {
        disconnect();
    }
}
