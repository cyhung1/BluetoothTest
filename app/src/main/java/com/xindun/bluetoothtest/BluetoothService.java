package com.xindun.bluetoothtest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.xindun.bluetoothtest.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by stuart on 2017/7/19.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";

    public static BluetoothService mInstance;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = BluetoothService.this;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    private ConnectThread connectThread;

    public void connectDevice(BluetoothDevice device, ConnectThread.onConnectListener l) {
        connectThread = new ConnectThread(device.getAddress());
        connectThread.addListener(l);
        connectThread.start();
    }

    public void sendMessage(byte[] bt) {
        if (connectThread != null) connectThread.sendMessage(bt);
    }


    public interface OnLeChangeListener {
        public void onLeConnect();

        public void onLeDisconnect();

        public void onRWChange(boolean canRw);

        public void onMsgRec(byte[] bt);
    }

    private List<OnLeChangeListener> mOnLeChangeListeners;

    private boolean canRW;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mOnLeChangeListeners != null) {
                    for (OnLeChangeListener l : mOnLeChangeListeners) {
                        l.onLeConnect();
                    }
                }
                LogUtil.i(TAG, "", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                canRead = false;
                canWrite = false;
                if (mOnLeChangeListeners != null) {
                    for (OnLeChangeListener l : mOnLeChangeListeners) {
                        l.onLeDisconnect();
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtil.i(TAG, "onServicesDiscovered", status +"" );
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices();
            }
            if (mOnLeChangeListeners != null) {
                for (OnLeChangeListener l : mOnLeChangeListeners) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        l.onRWChange(true);

                    } else {
                        l.onRWChange(false);
                        canRW = false;

                    }
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            LogUtil.i(TAG, "onCharacteristicRead", gatt +" status = " + status );
            if (status == BluetoothGatt.GATT_SUCCESS) {
                recMsg(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            LogUtil.i(TAG, "onCharacteristicChanged", gatt +"");
            recMsg(characteristic);
        }
    };

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");

    private void recMsg(final BluetoothGattCharacteristic characteristic) {

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        byte[] bt = null;
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                LogUtil.d(TAG, "", "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                LogUtil.d(TAG, "", "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            LogUtil.d(TAG, "", String.format("Received heart rate: %d", heartRate));

            bt = String.valueOf(heartRate).getBytes();
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for(byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
                bt = new String(data).getBytes();
            }
            if (TextUtils.isEmpty(new String(data).trim())) {
                return;
            }
        }

        if (mOnLeChangeListeners != null) {
            for (OnLeChangeListener l : mOnLeChangeListeners) {
                l.onMsgRec(bt);
            }
        }

    }


    private BluetoothAdapter mBluetoothAdapter;

    private String mBluetoothDeviceAddress;

    private BluetoothGatt mBluetoothGatt;

    public boolean connect(final String address, OnLeChangeListener l) {
        if (mOnLeChangeListeners == null) {
            mOnLeChangeListeners = new ArrayList<>();
        }
        mOnLeChangeListeners.clear();
        mOnLeChangeListeners.add(l);
        if (mBluetoothAdapter == null || address == null) {
            LogUtil.w(TAG, "connect " + address, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            LogUtil.d(TAG, "connect", "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                LogUtil.d(TAG, "connect", "connectiong");
                return true;
            } else {
                LogUtil.d(TAG, "connect", "faile");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            LogUtil.w(TAG, "connect", "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtil.d(TAG, "", "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;

        return true;
    }

    public void writeCharacteristic(byte[] msg) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "writeCharacteristic", "BluetoothAdapter not initialized");
            return;
        }
        LogUtil.w(TAG, "writeCharacteristic", "msg = " + msg);
        if (writeBluetoothGattCharacteristic == null) {
            LogUtil.w(TAG, "writeCharacteristic", "writeBluetoothGattCharacteristic == null");
            return;
        }
        writeBluetoothGattCharacteristic.setValue(msg);
        mBluetoothGatt.writeCharacteristic(writeBluetoothGattCharacteristic);
    }

    private BluetoothGattCharacteristic writeBluetoothGattCharacteristic;


    boolean canWrite = false;
    boolean canRead = false;
    private void displayGattServices() {
        LogUtil.i(TAG, "displayGattServices", "mBluetoothGatt = " + mBluetoothGatt);
        if (mBluetoothGatt == null) return;

        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
        LogUtil.i(TAG, "displayGattServices", "gattServices = " + gattServices);
        if (gattServices == null) {

            return;
        }

        LogUtil.i(TAG, "displayGattServices", "gattServices.size = " + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString().toLowerCase();
            LogUtil.i(TAG, "displayGattServices", "uuid = " +uuid);

            if (uuid.contains("18ff")) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                        .getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    String id = gattCharacteristic.getUuid().toString()
                            .toLowerCase();
                    LogUtil.i("stuart", "BluetoothGattCharacteristic id = " , id);
                    if (id.contains("2aff")) {
                        writeBluetoothGattCharacteristic = gattCharacteristic;
                        canWrite = true;
                    }
                    if (id.contains("2afe")) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            if (mBluetoothGatt != null)
                                mBluetoothGatt.readCharacteristic(gattCharacteristic);

                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                                LogUtil.w(TAG, "", "BluetoothAdapter not initialized");
                                return;
                            }
                            mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);

                            // This is specific to Heart Rate Measurement.
                            LogUtil.i(TAG, "stuart", " gattCharacteristic.getUuid() = " + gattCharacteristic.getUuid() +", description = " + gattCharacteristic.getDescriptor(
                                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")));
//                            00002afe-0000-1000-8000-00805f9b34fb
                         /*   if (UUID_HEART_RATE_MEASUREMENT.equals(gattCharacteristic.getUuid())) {
                                BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(
                                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);
                            }*/

                            BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(
                                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);
                            }


                            canRead = true;
                           /* new Thread(){
                                @Override
                                public void run() {
                                    while (canRead) {
                                        readCharacteristic(gattCharacteristic);
                                    }
                                }
                            }.start();*/
                        }

                    }
                }
                break;
            }
        }

    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.i(TAG, "readCharacteristic","BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
        mInstance = null;
    }

    public void close() {
        LogUtil.i(TAG, "close","close  mBluetoothGatt");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}




