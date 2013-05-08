package com.mp3record;

import java.util.List;

public enum ConfigType {
	None("",			""),
    Mp3Filename("",		""),
    Host("",			"smtp.gmail.com"),
    User("",			"user@gmail.com"),
    AuthUser("",		"gmail username"),
    AuthPwd("",			"gmail password"),
    Port("",			"gmail port... 465"),
    SendTo("",			"send to list, ... user1@gmail.com, user2@gmail.com,..."),
    FromUser("",		"me@gmail.com"),
    Subject("",			"'subject of email'"),
    RecordStartTime	("","auto record start time,  HH:mm:ss"), 
    RecordEndTime("",	"auto record end   time,  HH:mm:ss"),
    AutoExitTime("",	"Exit app after this time,HH:mm:ss");
	
    private String value;
    private String description;
    
	ConfigType (String value, String description) {
		setValue(value);
		this.description = description;
	}
	public String getValue() {
		return value;
	}
	public String getDescription() {
		return description;
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
	public static ConfigType getConfigType (String line)
	{
		if (!line.startsWith(";")) {
			for (ConfigType t : ConfigType.values()) {
				if (line.startsWith(t.name()))
					return t;
			}
		}
		return None;
	}
	public static boolean isValid ()
	{
		for (ConfigType t : ConfigType.values()) {
			if (t == None)
				continue;
			if (!t.getValue().isEmpty())
				return true;
		}
		return false;
	}
}
