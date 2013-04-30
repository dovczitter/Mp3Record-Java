package com.mp3record;

import java.io.File;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnClickListener {

	private TextView tvStatus;
	private TextView tvFilename;
	private Button bRecord;
	private Button bEmailMp3;
	private Button bEmailFile;
	private Button bHelp;
	private Button bExit;
	
	private AlertDialog.Builder exitConfirm;
	private AlertDialog.Builder emailMp3Confirm;
	private AlertDialog.Builder emailFileConfirm;
	private Animation blinker;
	private Chronometer recordTime;
	private Chronometer currentTime;
	private boolean fromShowHelp = false;
	private static String appName = "";
	private Util util = Util.getInstance();
	private boolean isTimedRecording = false;

	//
	// =================================================================================
	//
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		setAppName();
		util.setMp3Config();
		setRecordButton();
		setEmailMp3Button ();
		setEmailFileButton ();
		setHelpButton ();
		setExitButton ();
		setExitConfirm();
		setEmailMp3Confirm();
		setEmailFileConfirm();
		getCurrentTime().start();
				
		util.getMp3Lame().setHandle (new Handler() {
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
	//
	// =================================================================================
	//
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
				emailMp3Confirm.setMessage
					("Do you really want to email '"+getTvFilename().getText().toString()+
					 "' \nSendTo: "+ConfigType.SendTo.getValue()+" ???");
				emailMp3Confirm.create().show();
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
				getFiles (new File(util.getDirPath()).listFiles());
			} 
		});
	}
	private Button getHelpButton()
	{
		if (bHelp == null)
			bHelp = (Button) findViewById(R.id.HelpButton);
		return bHelp;
	}
	private void setHelpButton ()
	{
		getHelpButton().setOnClickListener(new View.OnClickListener() { 
			public void onClick(View view) {
				showHelp();
			} 
		});
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
				exitConfirm.create().show();
			} 
		});
	}

	private void setExitConfirm()
	{
		exitConfirm = new AlertDialog.Builder(this);
		exitConfirm.setTitle("'Exit' confirmination.");
		exitConfirm.setMessage("Do you really want to exit?");
		exitConfirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	System.exit(0);
	        } });
		exitConfirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        } });
	}

	private void setEmailMp3Confirm()
	{
		emailMp3Confirm = new AlertDialog.Builder(this);
		emailMp3Confirm.setTitle("'Email Mp3' confirmination.");
		emailMp3Confirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	util.sendMp3 (util.getMp3Pathname(), isOnline());
	        	dialog.dismiss();
	        } });
		emailMp3Confirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        } });
	}
	
	private void setEmailFileConfirm()
	{
		emailFileConfirm = new AlertDialog.Builder(this);
		emailFileConfirm.setTitle("'Email File' confirmination.");
		emailFileConfirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	util.sendFile (util.getFilePathName(), isOnline());
	        	dialog.dismiss();
	        } });
		emailFileConfirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	setListAdapter(null);
	            dialog.dismiss();
	        } });
	}
	
	private Chronometer getRecordTime()
	{
		if (recordTime == null)
		{
			recordTime = (Chronometer) findViewById(R.id.RecordTime);
			recordTime.setBase(SystemClock.elapsedRealtime());
			recordTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
	        @Override
	        public void onChronometerTick(Chronometer chronometer) {
	            CharSequence text = chronometer.getText();
	            if (text.length()  == 4)
	                chronometer.setText("0"+text);
        	}});
		}
		return recordTime;
	}
	private Chronometer getCurrentTime()
	{
		if (currentTime == null)
		{
			currentTime = (Chronometer) findViewById(R.id.CurrentTime);
			currentTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
	        @Override
	        public void onChronometerTick(Chronometer chronometer) {
	        	String currentTimeStamp = util.getCurrentTimeStamp();
                chronometer.setText(currentTimeStamp);
	            if (util.isConfigured()) {
	            	bEmailMp3.setText(isOnline() ? R.string.bEmailAvailable : R.string.bEmailNoWifi);
	            	if (!isTimedRecording && util.isRecordStart ()) {
	            		startRecording();
	            		isTimedRecording = true;
	            	}
	            	else if (isTimedRecording && util.isRecordEnd ()) {
		            	stopRecording();
	            		isTimedRecording = false;
	            	}
	            }
        		bEmailFile.setText(getEmailFileButtonText());
	        } });
		}
		return currentTime;
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
	private void setAppName()
	{
		appName = getString(R.string.app_name);
	}
	public static String getAppName()
	{
		return appName;
	}

	private void startRecording ()
	{
		util.clearMp3Pathname();
		getRecordButton().setBackgroundResource (R.drawable.round_stop_button);
		getRecordButton().setText (R.string.bStop);
		getRecordButton().startAnimation (getBlinker());
		getRecordTime().setBase (SystemClock.elapsedRealtime());
		getRecordTime().start();
		getTvStatus().setText (MsgType.RecStarted.name());
		util.getMp3Lame().setFilePath (util.getMp3Pathname());
		util.getMp3Lame().start();
		getTvFilename().setText ("["+Util.getFilenameFromPath (util.getMp3Pathname())+"]");
	}

	private void stopRecording ()
	{
		getRecordButton().clearAnimation();
		getRecordButton().setBackgroundResource (R.drawable.round_start_button);
		getRecordButton().setText (R.string.bStart);
		getRecordTime().stop();
		getTvStatus().setText (MsgType.RecStopped.name());
		util.getMp3Lame().stop();
	}

	private boolean isOnline()
	{
		ConnectivityManager manager =
				(ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = manager.getActiveNetworkInfo();
		return (network != null && network.isConnected());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		util.getMp3Lame().stop();
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	
	} 
    protected void onListItemClick(ListView l, View v, int position, long selectedRow)
    {
    	if (fromShowHelp) {
    		fromShowHelp = false;
    	}
    	else {
	        if(selectedRow == 0){
	        	getFiles (new File(util.getDirPath()).listFiles());
	        }else{
	            File file = new File((String)l.getItemAtPosition(position));
	            if(file.isDirectory()){
	                getFiles(file.listFiles());
	            }else{
	            	util.setFilePathName(file.getPath());
					emailFileConfirm.setMessage	(util.getFileAlertMessage ());
					emailFileConfirm.create().show();
	            }
	        }
    	}
        setListAdapter(null);
    }
    private void getFiles (File[] files)
    {
    	final String[] itemArray = util.getFileItems(files);
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle("Mp3 FILES");
    	b.setItems(itemArray, new DialogInterface.OnClickListener() {
    	        @Override
    	        public void onClick(DialogInterface arg0, int selectedRow)
    	        {
    		        if(selectedRow == 0){
    		        	getFiles (new File(util.getDirPath()).listFiles());
    		        }else{
    		            File file = new File(itemArray[selectedRow]);
    		            if (file.isDirectory()) {
    		                getFiles (file.listFiles());
    		            }else{
    		            	util.setFilePathName (file.getPath());
    						emailFileConfirm.setMessage (util.getFileAlertMessage ());
    						emailFileConfirm.create().show();
    		            }
    		        }
    	        }
    	    });

    	AlertDialog alert = b.create();
    	alert.show();
    }

    private void showHelp()
    {
    	fromShowHelp = true;
    	String[] itemArray = util.getHelpItems();
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle("Mp3 HELP");
    	b.setItems(itemArray, new DialogInterface.OnClickListener() {
    	        @Override
    	        public void onClick(DialogInterface arg0, int choosenAddress) {
    	        }
    	    });

    	AlertDialog alert = b.create();
    	alert.show();
    }
}