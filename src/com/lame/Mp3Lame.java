package com.lame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.widget.Toast;

import com.lame.LameWrapper;
import com.mp3record.MsgType;

public class Mp3Lame extends Activity {

	static {
		System.loadLibrary("mp3lame");
	}
	private String mFilePath;
	private static int mSampleRate = 16000;
	private boolean mIsRecording = false;
	private Handler mHandler;
	
	public void setFilePath (String filePath) {
		this.mFilePath = filePath;
	}
	public Mp3Lame (String filePath)
	{
		this.mFilePath = filePath;
	}

	public void start()
	{
		if (mIsRecording) {
			return;
		}
		new Thread() {
			@Override
			public void run() {
				processRecord();
			}
		}.start();
	}

	public void stop() {
		mIsRecording = false;
	}

	public boolean isRecording() {
		return mIsRecording;
	}
	
	public void setHandle(Handler handler) {
		this.mHandler = handler;
	}
	
	private void processRecord()
	{
		if (mHandler == null) {
			Toast.makeText (getBaseContext(),"processRecord() null Handle error.",Toast.LENGTH_LONG).show();
			return;
		}
		android.os.Process.setThreadPriority (android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		final int minBufferSize =
				AudioRecord.getMinBufferSize (mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (minBufferSize < 0) {
			mHandler.sendEmptyMessage (MsgType.ErrorGetMinBuffersize.getValue());
			return;
		}
		AudioRecord audioRecord =
				new AudioRecord (MediaRecorder.AudioSource.MIC, mSampleRate,
								 AudioFormat.CHANNEL_IN_MONO,
								 AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);
	
		// PCM buffer size (5sec)
		short[] buffer = new short [mSampleRate * (16 / 8) * 1 * 5]; // SampleRate[Hz] * 16bit * Mono * sec
		byte[] mp3buffer = new byte [(int) (7200 + buffer.length * 2 * 1.25)];
	
		FileOutputStream output = null;
		try {
			output = new FileOutputStream (new File(mFilePath));
		} catch (FileNotFoundException e) {
			mHandler.sendEmptyMessage (MsgType.ErrorCreateFile.getValue());
			return;
		}
	
		// Lame init
		/* outBitrate = 16, filesize of 100kb per minute */
		LameWrapper.init (mSampleRate, 1, mSampleRate, 16);
		
		mIsRecording = true;
		try {
			try {
				audioRecord.startRecording();
			} catch (IllegalStateException e) {
				mHandler.sendEmptyMessage (MsgType.ErrorRecStart.getValue());
				return;
			}
	
			try {
				mHandler.sendEmptyMessage (MsgType.RecStarted.getValue());
	
				int readSize = 0;
				while (mIsRecording) {
					readSize = audioRecord.read(buffer, 0, minBufferSize);
					if (readSize < 0) {
						mHandler.sendEmptyMessage (MsgType.ErrorAudioRecord.getValue());
						break;
					}
					else if (readSize == 0) {
						;
					}
					else {
						int encResult = LameWrapper.encode(buffer,
								buffer, readSize, mp3buffer);
						if (encResult < 0) {
							mHandler.sendEmptyMessage (MsgType.ErrorAudioEncode.getValue());
							break;
						}
						if (encResult != 0) {
							try {
								output.write(mp3buffer, 0, encResult);
							} catch (IOException e) {
								mHandler.sendEmptyMessage (MsgType.ErrorWriteFile.getValue());
								break;
							}
						}
					}
				}
	
				int flushResult = LameWrapper.flush(mp3buffer);
				if (flushResult < 0) {
					mHandler.sendEmptyMessage (MsgType.ErrorAudioEncode.getValue());
				}
				if (flushResult != 0) {
					try {
						output.write(mp3buffer, 0, flushResult);
					} catch (IOException e) {
						mHandler.sendEmptyMessage (MsgType.ErrorWriteFile.getValue());
					}
				}
	
				try {
					output.close();
				} catch (IOException e) {
					mHandler.sendEmptyMessage (MsgType.ErrorCloseFile.getValue());
				}
			} finally {
				audioRecord.stop();
				audioRecord.release();
			}
		} finally {
			LameWrapper.close();
			mIsRecording = false;
		}
	
		mHandler.sendEmptyMessage (MsgType.RecStopped.getValue());
	}
}
