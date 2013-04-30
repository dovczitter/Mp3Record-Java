package com.mp3record;

import java.util.List;

public enum ConfigType {
	None(""),
    Mp3Filename(""),
    Host(""),			// "smtp.gmail.com";
    User(""),			// "dovczitter@gmail.com";
    AuthUser(""),		// "dovczitter";
    AuthPwd(""),		// "dov1czitter";
    Port(""),			// "465";
    SendTo(""),			// "dovczitter@gmail.com";
    FromUser(""),		// "dovczitter@gmail.com";
    Subject(""),
    RecordStartTime(""),// auto record start time HH:mm:ss
    RecordEndTime("");	// auto record end   time HH:mm:ss
	
    private String value;
    
	ConfigType (String value)
	{
		setValue(value);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public static void setConfigTypes (List<String> lines)
	{
		for (String s : lines)
		{
			if (s.startsWith(";"))
				continue;
			for (ConfigType t : ConfigType.values())
			{
				if (s.startsWith(t.name()))
				{
					int startPos = s.indexOf(":") + 1;
					if (startPos >= 0) {
						t.setValue(s.substring(startPos).trim());
						continue;
					}
				}
			}
		}
	}
	public static boolean isValid ()
	{
		for (ConfigType t : ConfigType.values())
		{
			if (t == None)
				continue;
			if (!t.getValue().isEmpty())
				return true;
		}
		return false;
	}
}
