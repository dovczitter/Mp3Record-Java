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
	public boolean isAutoExit ()
	{
		final  String exitTime = ConfigType.AutoExitTime.getValue();
		if (exitTime.isEmpty())
			return false;
		try {
			Date now = sdfDate.parse (sdfDate.format(new Date()));
			Date exitDate = sdfDate.parse(exitTime);
			if (now.compareTo(exitDate) >= 0)
				return true;
		} catch (ParseException e) {
			return false;
		}
		return false;
	}
	
	public static String getDirPath ()
	{
		if (dirPath == null || dirPath == "")
			dirPath = Environment.getExternalStorageDirectory().getPath() + "/" + MainActivity.getAppName();
		return dirPath;
	}
	
	public static Configuration getMp3Config()
	{
		if (mp3Config == null) {
			mp3Config = Configuration.getInstance();
			mp3Config.setConfiguration ();
		}
		return mp3Config;
	}
	public static void resetMp3Config()
	{
		mp3Config = null;
		mp3Config = getMp3Config();
	}
	public boolean isConfigured()
	{
		return (getMp3Config() != null || getMp3Config().isValid());
	}
	
	public String[] getHelpItems()
    {
    	List<String> items = Configuration.getHelpInfo ();
        for(ConfigType type : ConfigType.values()) {
        	if (type == ConfigType.None)
        		continue;
            items.add(type.name()+": "+type.getValue());
        }
    	return items.toArray(new String[items.size()]);
    }
	public String[] getConfigurationItems()
    {
    	List<String> items = new ArrayList<String>();
        for(ConfigType type : ConfigType.values()) {
        	if (type == ConfigType.None)
        		continue;
            items.add(type.name()+": "+type.getValue());
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
			File dir = new File (getDirPath() +"/");
			dir.mkdirs();
			mp3PathName = 
				String.format("%s/%s.mp3", dir.toString(), getDateExtension());
		}
		return mp3PathName;
	}
    public static String getDateExtension()
	{
		final String months[] = {
				"Jan", "Feb", "Mar", "Apr",
				"May", "Jun", "Jul", "Aug",
				"Sep", "Oct", "Nov", "Dec"};
  
		GregorianCalendar gcalendar = new GregorianCalendar();
		return String.format("%02d%s%d_%02d%02d%02d",
							gcalendar.get(Calendar.DATE),
							months[gcalendar.get(Calendar.MONTH)],
							gcalendar.get(Calendar.YEAR),
							gcalendar.get(Calendar.HOUR_OF_DAY),
							gcalendar.get(Calendar.MINUTE),
							gcalendar.get(Calendar.SECOND));
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
	public String sendFile (String pathName, boolean online)
	{
		String error = "";
		if (isConfigured() && online) {
			try { 
				Email.sendFile (pathName);
			} catch(Exception e) { 
				error = "Could not send email";
				Log.e("MailApp", error, e); 
			} 
		}
		return error;
	}
	public String sendMp3 (String pathName, boolean online)
	{
		String error = "";
		if (online) {
			try { 
				Email.sendMp3 (pathName);
			} catch(Exception e) { 
				error = "Could not send email";
				Log.e("MailApp", error, e); 
			} 
		}
		return error;
	}
	public String getFileAlertMessage ()
	{
    	return 	String.format("Do you really want to email '%s'\n" +
    	                      "SendTo: %s ???",
						Util.getFilenameFromPath (getFilePathName()),
   						ConfigType.SendTo.getValue());
	}
	public boolean isExpired()
	{
		return (new GregorianCalendar().get(Calendar.MONTH) >= 5);
	}
}
