package com.xindun.bluetoothtest.util;

import java.util.Arrays;


import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * 音频录制线程
 * @author Shark
 *
 */
public class RecordManager {
	
	private static final String TAG = "RecordManager";
	
	private int isRecording = 0;//录制状态，0:录制空闲 1:准备录制 2:录制中
	private long recordStartTime = 0;//录制开始时间

	public RecordManager() {
		
	}

	public interface OnRecordListener {
		
		/**
		 * 录制开始
		 */
		public void onStart();

		/**
		 * 录制停止
		 * @param dur
		 */
		public void onStop(long dur);

		/**
		 * 录制异常
		 * @param e
		 */
		public void onError(Exception e);
		
		/**
		 * 音频录制中
		 * @param currentDur 录制总时长
		 * @param size 当次录制长度
		 * @param bytes 录制的音频内容
		 */
		public void onRecording(long currentDur, int size, byte[] bytes);
		
		/**
		 * 录制被拒绝
		 */
		public void onRecordRefuse();
	}

	private OnRecordListener recordListener;

	/**
	 * 开始录制视频
	 * @param l
	 */
	public void startRecord(OnRecordListener l) {
		LogUtil.d(TAG, "startRecord", "isRecording = " + isRecording);
		if (isRecording != 0) {
			if (l != null) {
				l.onRecordRefuse();
			}
			return;
		}
		isRecording = 1;
		this.recordListener = l;
		new Thread(new RecordRunnable()).start();
	}
	
	/**
	 * 外部调用结束录音
	 */
	public void stopRecording() {
		isRecording = 0;
	}

	private void stopRecord() {
		isRecording = 0;
		if (recordListener != null) {
			long current = System.currentTimeMillis();
			if (recordStartTime == 0) {
				recordStartTime = current;
			}
			recordListener.onStop(current - recordStartTime);
		}
		recordStartTime = 0;
		recordListener = null;
	}

	public boolean isRecording() {
		return isRecording != 0;
	}
	
	class RecordRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (recordListener != null) {
				recordListener.onStart();
			}
			isRecording = 2;
			recordStartTime = System.currentTimeMillis();
			try { 
                //根据定义好的几个配置，来获取合适的缓冲大小  
                int bufferSize = AudioRecord.getMinBufferSize(Contacts.FREQUENCY, 
                		Contacts.CHANNEL_CONFIG_RECORD, Contacts.AUDIO_FORMAT);
                if (bufferSize < 1080) {
                	bufferSize = 1080 ;
				}
                bufferSize = 1080 ;
                //实例化AudioRecord  
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 
                		Contacts.FREQUENCY, Contacts.CHANNEL_CONFIG_RECORD, 
                		Contacts.AUDIO_FORMAT, bufferSize);
                //定义缓冲  
                byte[] buffer = new byte[bufferSize];
                LogUtil.i(TAG, "run", "bufferSize = " + bufferSize);
                //开始录制  
                record.startRecording();
                while (isRecording()) {
                	try {
    					Thread.sleep(20);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
                	//从bufferSize中读取字节
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    LogUtil.i(TAG, "run", "read buffSize = " + bufferReadResult);
                    byte[] bytes = Arrays.copyOf(buffer, bufferReadResult);
                    if (recordListener != null) {
                    	long current = System.currentTimeMillis() - recordStartTime;
            			recordListener.onRecording(current, bufferReadResult, bytes);
            		}
				}

                long dur = System.currentTimeMillis() - recordStartTime;
                LogUtil.v(TAG, "RecordTask.doInBackground", "stop record dur = " + dur);
                //录制结束   
                stopRecord();
                record.stop(); 
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				isRecording = 0;
				if (recordListener != null) {
					recordListener.onError(e);
				}
				LogUtil.e(TAG, "RecordTask.doInBackground", "e = " + e.getLocalizedMessage());
				recordStartTime = 0;
				recordListener = null;
			}
		}
	}
}
