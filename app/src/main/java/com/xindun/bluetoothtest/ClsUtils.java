package com.xindun.bluetoothtest;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class ClsUtils {

	// 自动配对设置Pin值
	static public boolean autoBond(Class btClass, BluetoothDevice device,
			String strPin) throws Exception {
		Method autoBondMethod = btClass.getMethod("setPin",
				new Class[] { byte[].class });
		Boolean result = (Boolean) autoBondMethod.invoke(device,
				new Object[] { strPin.getBytes() });
		return result;
	}

	// 开始配对
	static public boolean createBond(Class btClass, BluetoothDevice device)
			throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	static public boolean setPin(Class btClass, BluetoothDevice btDevice,
			String str) throws Exception {
		try {
			Method removeBondMethod = btClass.getDeclaredMethod("setPin",
					new Class[] { byte[].class });
			Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,
					new Object[] { str.getBytes() });
			Log.e("returnValue", "" + returnValue);
			System.out.println("returnValue" + returnValue);
		} catch (SecurityException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}
}
