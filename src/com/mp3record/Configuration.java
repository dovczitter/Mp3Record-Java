package com.mp3record;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
	
	private static Configuration instance = new Configuration();

    private Configuration() { 
    }
    public synchronized static Configuration getInstance() {
        return instance;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
    // =========================================================================

	public void setConfiguration () {
		ConfigType.setConfigTypes (getConfigLines());
	}

	public static boolean saveUserInput (String userInput, int selectedRow)
	{
		PrintWriter pw = null;
		boolean retval = false;
		try 
	    {
			String path = getConfigPathName();
			if (!userInput.isEmpty())
			{
				saveConfigFile ();
				BufferedReader reader = new BufferedReader(new FileReader(path));
				String line;
				List<String> lines = new ArrayList<String>();
				int lineNum = 0;
				while ((line = reader.readLine()) != null) {
					if (selectedRow == lineNum)
						lines.add (userInput);
					else
						lines.add (line);
					lineNum++;
				}
				reader.close();
				pw = new PrintWriter (new FileWriter(path));
				for (String l : lines) {
			        pw.println (l);
				}
				pw.flush();
				retval = true;
			}
		}
	    catch (Exception e) {
	    	retval = false;
	    }
	    finally {
	    	if (pw != null)
	    		pw.close();
	    }
		return retval;
	}
	private static void saveConfigFile ()
	{
		try {
			String fromFile = getConfigPathName();
			String toFile = fromFile +"_"+ Util.getDateExtension();
			BufferedReader reader = new BufferedReader(new FileReader(fromFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(toFile));

			String line = null;
			while ((line=reader.readLine()) != null) {
				writer.write(line);
				writer.newLine();
			}
			reader.close();
			writer.close();
		}
		catch (Exception e) {
		}
    }
	public boolean isValid()
	{
		return ConfigType.isValid();
	}
	
	private static List<String> info;
    private static String configPathName;
    
	public static List<String> getHelpInfo ()
	{
		if (info == null)
		{
			info = new ArrayList<String>();
			info.add (String.format("; ============================================== "));
			info.add (String.format("; [ %s ]",getConfigPathName()));
			info.add (String.format("; - ';' designates a comment line."));
			info.add (String.format(";                                                "));
			info.add (String.format("; - EMAIL REQUIRED FIELDS: "));
			info.add (String.format(";   %s: /%s/",ConfigType.Host.name(), ConfigType.Host.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.User.name(), ConfigType.User.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.AuthUser.name(), ConfigType.AuthUser.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.AuthPwd.name(), ConfigType.AuthPwd.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.Port.name(), ConfigType.Port.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.SendTo.name(), ConfigType.SendTo.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.FromUser.name(), ConfigType.FromUser.getDescription()));
			info.add (String.format(";                                                "));
			info.add (String.format("; - OPTIONAL FIELDS: "));
			info.add (String.format(";   %s: /%s/",ConfigType.Subject.name(), ConfigType.Subject.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.RecordStartTime.name(), ConfigType.RecordStartTime.getDescription()));
			info.add (String.format(";   %s: /%s/",ConfigType.RecordEndTime.name(), ConfigType.RecordEndTime.getDescription()));
			info.add (String.format("; ============================================== "));
		}
		return info;
	}
	public static List<String> getConfigLines ()
	{
		List<String> lines = new ArrayList<String>();
		try {
			String configPathName = getConfigPathName();
			File f = new File(configPathName);
			if (!f.exists()) {
				PrintWriter pw = new PrintWriter (new FileWriter(configPathName));
				for (String s : getHelpInfo ()) {
					pw.println (s);
				}
				pw.flush();
				pw.close();
			}
			BufferedReader reader = new BufferedReader(new FileReader(configPathName));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		}
		catch (Exception e) { }
		return lines;
	}
    public static String getConfigPathName()
	{
    	if (configPathName == null)
    		configPathName = Util.getDirPath()+"/"+MainActivity.getAppName()+".cfg";
		return configPathName;
	}

}
