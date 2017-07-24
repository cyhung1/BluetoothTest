package com.xindun.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xindun.bluetoothtest.util.LogUtil;
import com.xindun.bluetoothtest.util.RecordManager;
import com.xindun.bluetoothtest.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "stuart";

    private Button btnSearch, record, stop_record;

    private ListView lvList;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothService mService;

    private RecordManager mRecordManager;

    private static boolean USE_BT_LE = true;

    private static final boolean TEST_DATA = true;

    private Button btB;

    private void checkBTB() {
        if (USE_BT_LE) btB.setText("BtLE");
        else btB.setText("BT");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, BluetoothService.class));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnSearch = (Button) findViewById(R.id.search);

        record = (Button) findViewById(R.id.record);

        btB = (Button) findViewById(R.id.bt_b);
        checkBTB();
        btB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USE_BT_LE = !USE_BT_LE;
                checkBTB();
            }
        });
        mRecordManager = new RecordManager();
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record.setEnabled(false);
                stop_record.setEnabled(true);
                if (TEST_DATA) {
                    testData = true;
                    new TestDataThreaed().start();
                } else
                    mRecordManager.startRecord(new RecordManager.OnRecordListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onStop(long dur) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }

                        @Override
                        public void onRecording(long currentDur, int size, byte[] bytes) {

                            LogUtil.i(TAG, "onRecording", "size " + size);
                            if (USE_BT_LE)
                                BluetoothService.mInstance.writeCharacteristic(bytes);
                            else
                                BluetoothService.mInstance.sendMessage(bytes);
                        }

                        @Override
                        public void onRecordRefuse() {

                        }
                    });
            }
        });
        stop_record = (Button) findViewById(R.id.stop_record);

        stop_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record.setEnabled(true);
                stop_record.setEnabled(false);
                if (TEST_DATA) {
                    testData = false;
                } else
                    mRecordManager.stopRecording();
            }
        });

        record.setEnabled(false);
        stop_record.setEnabled(false);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        lvList = (ListView) findViewById(R.id.lv);


        mAdapter = new LeDeviceListAdapter(this);
        lvList.setAdapter(mAdapter);

        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = ((LeDeviceListAdapter) adapterView
                        .getAdapter()).getDevice(i);
                LogUtil.i(TAG, "onItemClick", "device = " + device);
                if (device == null) {
                    return;
                }


                if (!USE_BT_LE)
                    BluetoothService.mInstance.connectDevice(device, new ConnectThread.onConnectListener() {
                        @Override
                        public void onConnect(String device) {
                            LogUtil.i(TAG, "onConnect", device);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ToastUtil.getInstance(MainActivity.this).show("连接成功");
                                    record.setEnabled(true);
                                }
                            });
                        }

                        @Override
                        public void onDisConnect() {
                            LogUtil.i(TAG, "onDisConnect", "");

                        }

                        @Override
                        public void onRead(byte[] buff) {
                            LogUtil.i(TAG, "onRead", "" + Arrays.toString(buff) +", time = " + format(System.currentTimeMillis()));
                        }

                        @Override
                        public void onConnectFailed(final String mac) {
                            LogUtil.i(TAG, "onConnectFailed", "" + mac);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ToastUtil.getInstance(MainActivity.this).show("连接失败," + mac);
                                    record.setEnabled(false);
                                }
                            });

                        }

                        @Override
                        public void onConnectStart() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ToastUtil.getInstance(MainActivity.this).show("正在连接...");
                                }
                            });

                        }
                    });
                else
                    BluetoothService.mInstance.connect(device.getAddress(), new BluetoothService.OnLeChangeListener() {
                        @Override
                        public void onLeConnect() {
                            LogUtil.i(TAG, "onConnect", "");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ToastUtil.getInstance(MainActivity.this).show("连接成功");
                                    record.setEnabled(true);
                                }
                            });
                        }

                        @Override
                        public void onLeDisconnect() {
                            LogUtil.i(TAG, "onConnectFailed", "");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ToastUtil.getInstance(MainActivity.this).show("连接失败,");
                                    record.setEnabled(false);
                                }
                            });
                        }

                        @Override
                        public void onRWChange(boolean canRw) {

                        }

                        @Override
                        public void onMsgRec(byte[] bt) {
                            LogUtil.i(TAG, "onMsgRec", "" + Arrays.toString(bt)+", time = " + format(System.currentTimeMillis()));
                        }
                    });
            }


        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "666", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mReceiver = new BluetoothReceiver();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter intentFilter = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private BluetoothReceiver mReceiver;

    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // listContainer.setVisibility(View.VISIBLE);

                mAdapter.addDevice(device);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("stuart", "STATE_OFF 手机蓝牙关闭");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("stuart", "STATE_TURNING_OFF 手机蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        search();
                        Log.d("stuart", "STATE_ON 手机蓝牙开启");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("stuart", "STATE_TURNING_ON 手机蓝牙正在开启");
                        break;
                }

            }
        }

    }


    private boolean scaning = false;
    private LeDeviceListAdapter mAdapter;
    private static final long SCAN_PERIOD = 10000;

    private void search() {
        btnSearch.setEnabled(false);
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            return;
        }

        ToastUtil.getInstance(MainActivity.this).show("正在搜索");

        mAdapter.clear();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (USE_BT_LE) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } else {
                    mBluetoothAdapter.cancelDiscovery();
                }

                btnSearch.setEnabled(true);
            }
        }, SCAN_PERIOD);

        scaning = true;

        if (USE_BT_LE) {

            mBluetoothAdapter.startLeScan(mLeScanCallback);

        } else {
            mBluetoothAdapter.startDiscovery();
        }


    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            if (device != null)
                mHandler.obtainMessage(MSG_WHAT_ADD_DEVICE, device).sendToTarget();
        }
    };

    private static final int MSG_WHAT_ADD_DEVICE = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_ADD_DEVICE:
                    mAdapter.addDevice((BluetoothDevice) msg.obj);
                    break;
                default:
                    break;
            }

        }
    };

    byte[] HEADR = new byte[]{(byte) 0xAA, 0x55};

    /**
     * 获取20位字节的数据
     *
     * @return
     */
    private byte[] getTestByte(int i) {

        byte[] bt = new byte[20];

        System.arraycopy(HEADR, 0, bt, 0, HEADR.length);
        System.arraycopy(new byte[]{(byte) i}, 0, bt, 2, 1);
        for (int x = 3; x < bt.length; x++) {
            bt[x] = (byte) new Random().nextInt(100);
        }

        return bt;

    }


    private boolean testData = false;

    private class TestDataThreaed extends Thread {
        @Override
        public void run() {
            int i = 0;
            while (testData) {

                if (USE_BT_LE)
                    BluetoothService.mInstance.writeCharacteristic(getTestByte(i++));
                else
                    BluetoothService.mInstance.sendMessage(getTestByte(i++));

                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static String format(long time) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return fmt.format(new Date(time));
    }
}


class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<BluetoothDevice> mPairDevices;
    private LayoutInflater mInflator;

    private static final int TYPE_TITLE = 9;
    private static final int TYPE_ITEM = 10;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mPairDevices = new ArrayList<BluetoothDevice>();
        mInflator = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device) {

        if (!mLeDevices.contains(device)) {

            mLeDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public void addPairDevice(BluetoothDevice device) {
        if (!mPairDevices.contains(device)) {
            mPairDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        if (mPairDevices.size() > 0) {
            if (position == 0)
                return null;
            else if (position <= mPairDevices.size())
                return mPairDevices.get(position - 1);
            else if (position == mPairDevices.size() + 1)
                return null;
            else
                return mLeDevices.get(position - mPairDevices.size() - 2);
        } else {
            if (position == 0)
                return null;
            else
                return mLeDevices.get(position - 1);
        }
    }

    public void clear() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    public void clearAll() {
        mPairDevices.clear();
        clear();
    }

    @Override
    public int getCount() {
        if (mLeDevices.size() == 0 && mPairDevices.size() == 0) {
            return 0;
        } else if (mLeDevices.size() > 0 && mPairDevices.size() == 0) {
            return mLeDevices.size() + 1;
        } else if (mPairDevices.size() > 0 && mLeDevices.size() == 0) {
            return mPairDevices.size() + 1;
        }
        return mLeDevices.size() + mPairDevices.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_TITLE;
        if (mPairDevices.size() > 0) {
            if (position == mPairDevices.size() + 1)
                return TYPE_TITLE;
        }
        return TYPE_ITEM;
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

		/*
         * @Override public int getViewTypeCount() { return 2; }
		 */

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        // General ListView optimization code.
        int itemViewType = getItemViewType(i);
        if (itemViewType == TYPE_ITEM) {
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceIcon = (ImageView) view
                        .findViewById(R.id.device_icon);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
                if (viewHolder == null) {
                    view = mInflator
                            .inflate(R.layout.listitem_device, null);
                    viewHolder = new ViewHolder();
                    viewHolder.deviceAddress = (TextView) view
                            .findViewById(R.id.device_address);
                    viewHolder.deviceIcon = (ImageView) view
                            .findViewById(R.id.device_icon);
                    viewHolder.deviceName = (TextView) view
                            .findViewById(R.id.device_name);
                    view.setTag(viewHolder);
                }
            }
        } else {
            if (view != null && view instanceof TextView) {
                if (i == 0) {
                    if (mPairDevices.size() > 0) {
                        ((TextView) view).setText(R.string.list_pair_title);

                    } else
                        ((TextView) view).setText(R.string.list_title);
                } else {
                    ((TextView) view).setText(R.string.list_title);
                }
                return view;
            } else {
                TextView v = (TextView) mInflator.inflate(
                        R.layout.device_list_item_title, null);
                if (i == 0) {
                    if (mPairDevices.size() > 0) {
                        ((TextView) v).setText(R.string.list_pair_title);

                    } else
                        ((TextView) v).setText(R.string.list_title);
                } else {
                    ((TextView) v).setText(R.string.list_title);
                }
                return v;
            }
        }

        BluetoothDevice device = getDevice(i);
        int type = device.getType();
        if (type == BluetoothDevice.DEVICE_TYPE_LE) {
            viewHolder.deviceIcon
                    .setImageResource(R.mipmap.device_icon_bt1);
        } else {
            viewHolder.deviceIcon
                    .setImageResource(R.mipmap.device_icon_bt);
        }
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText("未知设备");
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        ImageView deviceIcon;
    }


}

