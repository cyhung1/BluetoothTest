package com.xindun.bluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.xindun.bluetoothtest.util.LogUtil;

public class ConnectThread extends Thread {
	private static final String TAG = "ConnectThread";

	String macAddress = "";
	
	private List<onConnectListener> ls;

	public ConnectThread(String mac) {
		macAddress = mac;
	}
	
	public interface onConnectListener{
		public void onConnect(String device);
		
		public void onDisConnect();
		
		public void onRead(byte[] buff);
		
		public void onConnectFailed(String mac);
		
		public void onConnectStart();
	}

	private boolean connecting, connected;

	private BluetoothAdapter mBluetoothAdapter;

	private BluetoothDevice mBluetoothDevice;

	private BluetoothSocket socket;

	private int connetTime = 0;
	
	private InputStream inputStream;
	private OutputStream outputStream;

	public void run() {
		if (ls !=null) {
			for (onConnectListener l : ls)
				if (l != null) l.onConnectStart();
		}
		connecting = true;
		connected = false;
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
		mBluetoothAdapter.cancelDiscovery();

		if (mBluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {// 判断给定地址下的device是否已经配对
			try {
				ClsUtils.setPin(mBluetoothDevice.getClass(), mBluetoothDevice, "1234" );
				/*ClsUtils.autoBond(mBluetoothDevice.getClass(),
						mBluetoothDevice, "1234");// 设置pin值
				ClsUtils.createBond(mBluetoothDevice.getClass(),
						mBluetoothDevice);*/
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("配对不成功");
			}
		} else {
		}
		try {
			UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
			socket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
			if (!socket.isConnected())
			socket.connect();
			if (ls !=null) {
				for (onConnectListener l : ls)
					if (l != null) l.onConnect(macAddress);
			}
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			Log.d(TAG, "inputStream = " + inputStream);
			/*byte[] bytes = "$CCRCI,0,*68".getBytes();//ArrayUtil.getBTHeaderByte(DKJManager.getInstance().getInit());
			byte[] a = new byte[bytes.length + 2];
			System.arraycopy(bytes, 0, a, 0, bytes.length);
			a[bytes.length] = Contacts.COMMAND_CR;
			a[bytes.length + 1] = Contacts.COMMAND_LF;
			byte[] b = ArrayUtil.getBTHeaderByte(a);
			Log.d(TAG, "inputStream data is " + new String(bytes));
			outputStream.write(bytes);
			outputStream.flush();
			;*/
			byte[] bs = new byte[1024 * 8];
			while(true) {
				if (inputStream == null) {
					LogUtil.d(TAG, "run",  "inputStream is null");
					return;
				}
				int count = inputStream.read(bs);
				if(count > 0) {
					byte[] bytes = Arrays.copyOfRange(bs, 0, count);
					LogUtil.d(TAG, "run", "read count = " + count + ",data.size = " + bytes.length +
							",toString size = " + new String(bytes).length() +", data = " + new String(bytes));
					if (ls !=null) {
						for (onConnectListener l : ls)
							if (l != null) l.onRead(bytes);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e(TAG, "Socket", e);
			if (!connecting) return;
			if (ls !=null) {
				for (onConnectListener l : ls)
					if (l != null) l.onConnectFailed(macAddress);
			}
		}
		// adapter.cancelDiscovery();
		/*while (!connected && connetTime <= 10) {
			connectDevice();
		}*/
		// 重置ConnectThread
		// synchronized (BluetoothService.this) {
		// ConnectThread = null;
		// }
	}

	public void cancel() {
		try {
			LogUtil.d(TAG, "cancel", "begin");
			connecting = false;
			if (outputStream != null)
			outputStream.close();
			if (inputStream != null)
			inputStream.close();
			socket.close();
			outputStream = null;
			inputStream = null;
			socket = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ls !=null) {
				for (onConnectListener l : ls)
					if (l != null) l.onDisConnect();
			}
		}
	}
	
	public void addListener(onConnectListener l) {
		if (ls == null) ls = new ArrayList<onConnectListener>();
		ls.add(l);
	}
	
	public void removeListener(onConnectListener l) {
		if (ls != null) ls.remove(l);
	}

	protected void connectDevice() {
		try {
			// 连接建立之前的先配对
			if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
				Method creMethod = BluetoothDevice.class
						.getMethod("createBond");
				Log.e("TAG", "开始配对");
				creMethod.invoke(mBluetoothDevice);
			} else {
			}
		} catch (Exception e) {
			// TODO: handle exception
			// DisplayMessage("无法配对！");
			e.printStackTrace();
		}
		mBluetoothAdapter.cancelDiscovery();
		try {
			socket.connect();
			// DisplayMessage("连接成功!");
			// connetTime++;
			connected = true;
		} catch (IOException e) {
			// TODO: handle exception
			// DisplayMessage("连接失败！");
			connetTime++;
			connected = false;
			try {
				socket.close();
				socket = null;
			} catch (IOException e2) {
				// TODO: handle exception
				Log.e(TAG, "Cannot close connection when connection failed");
			}
		} catch (NullPointerException e) {

		} finally {
			connecting = false;
		}
	}
	
	public void sendMessage(final byte[] buffer) {
		LogUtil.d(TAG, "sendMessage", "outputStream = " + outputStream + " data = " + new String(buffer));
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
					try {
						if (outputStream != null) {

							outputStream.write(buffer);
							outputStream.flush();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/*try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/

			}
		}).start();
	}
	
	
	private void unpairDevice(BluetoothDevice device) {
		try {
		Method m = device.getClass()
		.getMethod("removeBond", (Class[]) null);
		m.invoke(device, (Object[]) null);
		} catch (Exception e) {
		Log.e(TAG, e.getMessage());
		}
		}
}
