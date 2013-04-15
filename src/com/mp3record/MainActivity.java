package com.mp3record;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.email.Email;
import com.lame.Mp3Lame;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.app.ListActivity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnClickListener {

	private static TextView tvStatus;
	private static TextView tvFilename;
	private static Button bRecord;
	private static Button bEmailMp3;
	private static Button bEmailFile;
	private static Button bExit;
	private static Configuration mp3Config;
	private Animation blinker;
	private Mp3Lame mp3Lame;
	private Chronometer chronometer;
	private String dirPath = "";
	private static String mp3PathName = "";
	private List<String> items = null;
	
	private Button getRecordButton()
	{
		if (bRecord == null)
			bRecord = (Button) findViewById(R.id.RecordButton);
		return bRecord;
	}
	private void setRecordButton()
	{
		getRecordButton().setText (R.string.bStart);
		getRecordButton().setBackgroundResource (R.drawable.round_start_button);
		getRecordButton().setOnClickListener (new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getRecordButton().getText().equals(getString(R.string.bStart))) {
					startRecording ();
				}
				else if (getRecordButton().getText().equals(getString(R.string.bStop))) {
					stopRecording ();
				}
			}
		});
	}
	private Button getEmailMp3Button()
	{
		if (bEmailMp3 == null)
			bEmailMp3 = (Button) findViewById(R.id.EmailMp3Button);
		return bEmailMp3;
	}
	private void setEmailMp3Button ()
	{
		getEmailMp3Button().setText(isOnline() ? R.string.bEmailAvailable : R.string.bEmailNoWifi);
		getEmailMp3Button().setOnClickListener(new View.OnClickListener() { 
			public void onClick(View view) {
				sendMp3 (getMp3Pathname());
			} 
		});
	}
	private Button getEmailFileButton()
	{
		if (bEmailFile == null)
			bEmailFile = (Button) findViewById(R.id.EmailFileButton);
		return bEmailFile;
	}
	private String getEmailFileButtonText()
	{
		String text = (isOnline() ? getString(R.string.bEmailFileAvailable) : getString(R.string.bEmailFileNoWifi));
		return text;
	}
	private void setEmailFileButton ()
	{
		getEmailFileButton().setText(getEmailFileButtonText());
		getEmailFileButton().setOnClickListener(new View.OnClickListener() { 
			public void onClick(View view) {
				getFiles (new File(getDirPath()).listFiles());
			} 
		});
	}
	private void sendFile (String pathName)
	{
		if (isConfigured() && isOnline()) {
			try { 
				Email.sendFile (pathName);
			} catch(Exception e) { 
				//Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show(); 
				Log.e("MailApp", "Could not send email", e); 
			} 
		}
	}
	private void sendMp3 (String pathName)
	{
		if (isOnline()) {
			try { 
				Email.sendMp3 (pathName);
			} catch(Exception e) { 
				//Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show(); 
				Log.e("MailApp", "Could not send email", e); 
			} 
		}
	}
	private Button getExitButton()
	{
		if (bExit == null)
			bExit = (Button) findViewById(R.id.ExitButton);
		return bExit;
	}
	private void setExitButton ()
	{
		getExitButton().setOnClickListener(new View.OnClickListener() { 
			public void onClick(View view) {
				System.exit(0);
			} 
		});
	}

	private Chronometer getChronometer()
	{
		if (chronometer == null)
		{
			chronometer = (Chronometer) findViewById(R.id.chronometer);
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
	        @Override
	        public void onChronometerTick(Chronometer chronometer) {
	            CharSequence text = chronometer.getText();
	            if (text.length()  == 4)
	                chronometer.setText("0"+text);
	            if (isConfigured())
	            	bEmailMp3.setText(isOnline() ? R.string.bEmailAvailable : R.string.bEmailNoWifi);
            		bEmailFile.setText(getEmailFileButtonText());
	        	}
			});

		}
		return chronometer;
	}
	private TextView getTvStatus ()
	{
		if (tvStatus == null)
			tvStatus = (TextView) findViewById(R.id.tvStatus);
		return tvStatus;
	}
	private TextView getTvFilename ()
	{
		if (tvFilename == null)
			tvFilename =(TextView) findViewById(R.id.tvFilename);
		return tvFilename;
	}
	private Configuration getMp3Config()
	{
		if (mp3Config == null)
			mp3Config = new Configuration (getDirPath());
		return mp3Config;
	}

	private String getDirPath()
	{
		if (dirPath == null || dirPath == "")
			dirPath = Environment.getExternalStorageDirectory().getPath() + "/" + getAppName();
		return dirPath;
	}
	private String getMp3Pathname()
	{
		if (mp3PathName == null || mp3PathName == "")
		{
			final String months[] = {
					"Jan", "Feb", "Mar", "Apr",
					"May", "Jun", "Jul", "Aug",
					"Sep", "Oct", "Nov", "Dec"};
	  
			GregorianCalendar gcalendar = new GregorianCalendar();
			File dir = new File(getDirPath() +"/");
			dir.mkdirs();
			mp3PathName = String.format("%s/%02d%s%d_%02d%02d%02d.mp3",
									dir.toString(),
									gcalendar.get(Calendar.DATE),
									months[gcalendar.get(Calendar.MONTH)],
									gcalendar.get(Calendar.YEAR),
									gcalendar.get(Calendar.HOUR_OF_DAY),
									gcalendar.get(Calendar.MINUTE),
									gcalendar.get(Calendar.SECOND));
		}
		return mp3PathName;
	}
	private String getFilenameFromPath (String filepath)
	{
		int start = filepath.lastIndexOf("/") + 1;
		return filepath.substring(start);
	}
	private Animation getBlinker()
	{
		if (blinker == null)
		{
			blinker = new AlphaAnimation(1, 0);
			blinker.setDuration(1000);
			blinker.setInterpolator(new LinearInterpolator());
			blinker.setRepeatCount(Animation.INFINITE);
			blinker.setRepeatMode(Animation.REVERSE);
		}
		return blinker;
	}
	private Mp3Lame getMp3Lame()
	{
		if (mp3Lame == null)
			mp3Lame = new Mp3Lame (getMp3Pathname());
		return mp3Lame;
	}
	private String getAppName()
	{
		return getString(R.string.app_name);
	}

	// =================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		setRecordButton();
		setEmailMp3Button ();
		setEmailFileButton ();
		setExitButton ();
		
		getMp3Lame().setHandle (new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				MsgType msgType = MsgType.getType(msg.what);
				
				getTvStatus().setText(msgType.name());

				if (msgType!=MsgType.None && msgType!=MsgType.RecStarted && msgType!=MsgType.RecStopped)
					Toast.makeText(MainActivity.this, msgType.name(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void startRecording ()
	{
		getRecordButton().setBackgroundResource (R.drawable.round_stop_button);
		getRecordButton().setText (R.string.bStop);
		getRecordButton().startAnimation (getBlinker());
		getChronometer().setBase (SystemClock.elapsedRealtime());
		getChronometer().start();
		getTvStatus().setText (MsgType.RecStarted.name());
		getMp3Lame().setFilePath (getMp3Pathname());
		getMp3Lame().start();
		getTvFilename().setText (getFilenameFromPath (getMp3Pathname()));
	}

	private void stopRecording ()
	{
		getRecordButton().clearAnimation();
		getRecordButton().setBackgroundResource (R.drawable.round_start_button);
		getRecordButton().setText (R.string.bStart);
		getChronometer().stop();
		getTvStatus().setText (MsgType.RecStopped.name());
		getMp3Lame().stop();
	}

	private boolean isOnline()
	{
		ConnectivityManager manager =
				(ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = manager.getActiveNetworkInfo();
		return (network != null && network.isConnected());
	}
	private boolean isConfigured()
	{
		return (getMp3Config() != null || Configuration.isValid());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		getMp3Lame().stop();
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	
	} 
	//
	// Credits go to : http://www.remwebdevelopment.com/dev/a34/Directory-Browser-Application.html
	//
    protected void onListItemClick(ListView l, View v, int position, long id){
        int selectedRow = (int)id;
        if(selectedRow == 0){
        	getFiles (new File(getDirPath()).listFiles());
        }else{
            File file = new File(items.get(selectedRow));
            if(file.isDirectory()){
                getFiles(file.listFiles());
            }else{
				sendFile (file.getPath());
            }
        }
        setListAdapter(null);
    }
    private void getFiles(File[] files){
        items = new ArrayList<String>();
        items.add(getString(R.string.goto_root));
        for(File file : files){
            items.add(file.getPath());
        }
        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,R.layout.file_list_row, items);
        setListAdapter(fileList);
    }

}