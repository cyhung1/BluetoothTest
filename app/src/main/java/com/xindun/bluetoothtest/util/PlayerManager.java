package com.xindun.bluetoothtest.util;

import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * 音频播放
 * 
 * @author Shark
 *
 */
public class PlayerManager {

	private AudioTrack mAudioTrack = null;
	private int bufferSize = 0;

	private static final int MIN_BUFFER_SIZE = /* 1080 */1200;

	public PlayerManager() {
		bufferSize = AudioTrack.getMinBufferSize(Contacts.FREQUENCY,
				Contacts.CHANNEL_CONFIG_PLAY, Contacts.AUDIO_FORMAT);
		if (bufferSize < /* 800 * 4 */MIN_BUFFER_SIZE) {
			bufferSize = /* 800 * 4 */MIN_BUFFER_SIZE;
		}
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				Contacts.FREQUENCY, Contacts.CHANNEL_CONFIG_PLAY,
				Contacts.AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);
		mAudioTrack.flush();
		mAudioTrack.play();
	}

	public void play(byte[] bytes) {
		mAudioTrack.write(bytes, 0, bytes.length);
	}

	public void play(byte[] bytes, int postion, int length) {
		mAudioTrack.write(bytes, postion, length);
	}
}
