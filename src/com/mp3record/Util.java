package com.mp3record;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.email.Email;
import com.lame.Mp3Lame;

import android.os.Environment;
import android.util.Log;

public class Util {
	
	private static Util instance = new Util();

    private Util() { 
    }
    public synchronized static Util getInstance() {
        return instance;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
    // =========================================================================
    private static SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
    private static String dirPath;
    private static Configuration mp3Config;
    private static String filePathName;
	private static Mp3Lame mp3Lame;
	private static String mp3PathName;
    
	public String getCurrentTimeStamp() {
        Date now = new Date();
        return sdfDate.format(now);
    }
	public boolean isRecordStart ()
	{
		final  String startTime = ConfigType.RecordStartTime.getValue();
		final  String endTime = ConfigType.RecordEndTime.getValue();
		if (startTime.isEmpty() || endTime.isEmpty())
			return false;
		try {
			Date now = sdfDate.parse (sdfDate.format(new Date()));
			Date startDate = sdfDate.parse(startTime);
			Date endDate = sdfDate.parse(endTime);
			if (now.compareTo(startDate) >= 0 && now.compareTo(endDate) < 0)
				return true;
		} catch (ParseException e) {
			return false;
		}
		return false;
	}
	public boolean isRecordEnd ()
	{
		final  String startTime = ConfigType.RecordStartTime.getValue();
		final  String endTime = ConfigType.RecordEndTime.getValue();
		if (startTime.isEmpty() || endTime.isEmpty())
			return false;
		try {
			Date now = sdfDate.parse (sdfDate.format(new Date()));
			Date startDate = sdfDate.parse(startTime);
			Date endDate = sdfDate.parse(endTime);
			if (now.compareTo(startDate) >= 0 && now.compareTo(endDate) >= 0)
				return true;
		} catch (ParseException e) {
			return false;
		}
		return false;
	}
	public String getDirPath ()
	{
		if (dirPath == null || dirPath == "")
			dirPath = Environment.getExternalStorageDirectory().getPath() + "/" + MainActivity.getAppName();
		return dirPath;
	}
	
	public void setMp3Config()
	{
		if (mp3Config == null)
			mp3Config = new Configuration (getDirPath());
	}
	public Configuration getMp3Config()
	{
		if (mp3Config == null)
			mp3Config = new Configuration (getDirPath());
		return mp3Config;
	}
	
	public boolean isConfigured()
	{
		return (getMp3Config() != null || Configuration.isValid());
	}
	
    public String[] getHelpItems()
    {
    	getMp3Config();
    	List<String> items = new ArrayList<String>();
        items.add("< Mp3.cfg contents >");
        for(ConfigType type : ConfigType.values()) {
            items.add(""+type.name()+": "+type.getValue());
        }
    	return items.toArray(new String[items.size()]);
    }
    public String[] getFileItems(File[] files)
    {
    	List<String> items = new ArrayList<String>();
    	/* goto root */
        items.add("..");
        for(File file : files){
            items.add(file.getPath());
        }
    	return items.toArray(new String[items.size()]);
    }
    public void setFilePathName(String fn)
	{
		filePathName = fn;
	}
    public String getFilePathName()
	{
		return filePathName;
	}
    public Mp3Lame getMp3Lame()
	{
		if (mp3Lame == null)
			mp3Lame = new Mp3Lame (getMp3Pathname());
		return mp3Lame;
	}
    public String getMp3Pathname()
	{
		if (mp3PathName == null || mp3PathName == "")
		{
			final String months[] = {
					"Jan", "Feb", "Mar", "Apr",
					"May", "Jun", "Jul", "Aug",
					"Sep", "Oct", "Nov", "Dec"};
	  
			GregorianCalendar gcalendar = new GregorianCalendar();
			File dir = new File (getDirPath() +"/");
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
    public void clearMp3Pathname()
	{
		mp3PathName = "";
	}
	public static String getFilenameFromPath (String filepath)
	{
		int start = filepath.lastIndexOf("/") + 1;
		return filepath.substring(start);
	}
	public void sendFile (String pathName, boolean online)
	{
		if (isConfigured() && online) {
			try { 
				Email.sendFile (pathName);
			} catch(Exception e) { 
				//Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show(); 
				Log.e("MailApp", "Could not send email", e); 
			} 
		}
	}
	public void sendMp3 (String pathName, boolean online)
	{
		if (online) {
			try { 
				Email.sendMp3 (pathName);
			} catch(Exception e) { 
				//Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show(); 
				Log.e("MailApp", "Could not send email", e); 
			} 
		}
	}
	public String getFileAlertMessage ()
	{
    	return 	String.format("Do you really want to email '%s'\n" +
    	                      "SendTo: %s ???",
						Util.getFilenameFromPath (getFilePathName()),
   						ConfigType.SendTo.getValue());
	}
}
