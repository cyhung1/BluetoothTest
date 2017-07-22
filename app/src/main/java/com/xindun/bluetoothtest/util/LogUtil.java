package com.xindun.bluetoothtest.util;

import android.util.Log;

public class LogUtil {

	private static final boolean DEBUG = true;
	
	private static final String BASE_TAG = "Xindun";
	
	public static void v(String TAG, String method, String msg) {
		if (DEBUG) {
			Log.v(BASE_TAG, TAG + "." + method + "()..." + msg);
		}
	}
	
	public static void d(String TAG, String method, String msg) {
		if (DEBUG) {
			Log.d(BASE_TAG, TAG + "." + method + "()..." + msg);
		}
	}
	
	public static void i(String TAG, String method, String msg) {
		if (DEBUG) {
			Log.i(BASE_TAG, TAG + "." + method + "()..." + msg);
		}
	}
	
	public static void w(String TAG, String method, String msg) {
		if (DEBUG) {
			Log.w(BASE_TAG, TAG + "." + method + "()..." + msg);
		}
	}
	
	public static void e(String TAG, String method, String msg) {
		if (DEBUG) {
			Log.e(BASE_TAG, TAG + "." + method + "()..." + msg);
		}
	}
}
