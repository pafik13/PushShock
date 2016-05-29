package ru.lifeplus.pushshock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DeviceListFragment extends ListFragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public final int DEVICE_NOT_SELECTED = -1;

    private OnListFragmentInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * The {@link BluetoothAdapter} that will host the section contents.
     */
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mLeDevices;
    private int mCurrentPosition = DEVICE_NOT_SELECTED;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000 / 2;
    /**
     * The @Constant for result activity
     */
    private final static int REQUEST_ENABLE_BT = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mHandler == null) {
            mHandler = new Handler();
        }
        if (mLeDevices == null) {
            mLeDevices = new ArrayList<>();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        LeDeviceListAdapter adapter = new LeDeviceListAdapter(mLeDevices);
        setListAdapter(adapter);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if (adapter.getCount() > 0 && mCurrentPosition != DEVICE_NOT_SELECTED) {
            getListView().setItemChecked(mCurrentPosition, true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //TODO: Is it need?
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            mListener.onListFragmentInteraction((BluetoothDevice) parent.getAdapter().getItem(position));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mCurrentPosition = position;
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            mListener.onListFragmentInteraction((BluetoothDevice) l.getAdapter().getItem(position));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BluetoothDevice item);
    }

    @Override
    public void onRefresh() {
        initiateRefresh();
    }
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Remove all items from the ListAdapter, and then replace them with the new items
            LeDeviceListAdapter adapter = (LeDeviceListAdapter)getListAdapter();
            adapter.clear();
            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //mScanning = false;
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    onRefreshComplete();
                }
            }, SCAN_PERIOD);

            //mScanning = true;
            // Filters
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(ShockDevice.PUSH_SERVICE))
                    .build();
            List<ScanFilter> filters = new ArrayList<>();
            filters.add(scanFilter);
            // Settings
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, scanSettings, mScanCallback);
        } else {
            //mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        }
    }

    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete() {
        // Stop the refreshing indicator
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private ScanCallback mScanCallback = new ScanCallback(){

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LeDeviceListAdapter adapter = (LeDeviceListAdapter) getListAdapter();
                    adapter.addDevice(result.getDevice());
                    adapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            super.onBatchScanResults(results);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LeDeviceListAdapter adapter = (LeDeviceListAdapter) getListAdapter();
                    for (ScanResult result : results) {
                        adapter.addDevice(result.getDevice());
                    }
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getContext(), getScanErrorText(errorCode), Toast.LENGTH_SHORT).show();
        }
    };

    CharSequence getScanErrorText(int errorCode){
        CharSequence result;
        switch (errorCode){
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                result = "SCAN_FAILED_ALREADY_STARTED";
                break;
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                result = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
                break;
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                result = "SCAN_FAILED_INTERNAL_ERROR";
                break;
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                result = "SCAN_FAILED_FEATURE_UNSUPPORTED";
                break;
            case 5:
                result = "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES";
                break;
            default:
                result = "UNKNOWN ERROR";
                break;
        }
        return  result;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> devices;

        public LeDeviceListAdapter(ArrayList<BluetoothDevice> devices) {
            super();
            this.devices = devices;
        }

        public void addDevice(BluetoothDevice device) {
            if(!devices.contains(device)) {
                devices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            devices.clear();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = (convertView != null) ? convertView : LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.fragment_device, parent, false);
//
//            TextView mIdView = (TextView) view.findViewById(R.id.id);
//            TextView mContentView = (TextView) view.findViewById(R.id.content);
//            mIdView.setText(mLeDevices.get(position).getName());
//            mContentView.setText(mLeDevices.get(position).getAddress());

            View view = (convertView != null) ? convertView : LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_single_choice, parent, false);
            ((CheckedTextView) view).setText(devices.get(position).getName());

            return view;
        }
    }
}
