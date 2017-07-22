package com.xindun.bluetoothtest.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

	private static ToastUtil mInstace;

	private Toast mToast;
	private Toast mPreToast;// 之前的Toast

	private static Context mContext;

	private ToastUtil() {
	}

	public static ToastUtil getInstance(Context c) {
		if (mInstace == null)
			mInstace = new ToastUtil();
		mContext = c;
		return mInstace;
	}
	
	public void show(int resId) {
		show(mContext.getString(resId));
	}

	public void show(String text) {
		mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
		mToast.setDuration(1000);
		mToast.setText(text);
		mToast.show();

		if (mPreToast != null) {
			mPreToast.cancel();
		}
		mPreToast = mToast;
	}
	
	public void cancle() {
		if (mPreToast != null) {
			mPreToast.cancel();
		}
		mInstace = null;
	}
}
