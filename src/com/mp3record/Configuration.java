package com.mp3record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
	
	Configuration () {}
	Configuration (String dirPath)
	{
		ConfigType.setConfigTypes (getConfigLines(dirPath));
	}

	public static boolean isValid()
	{
		return ConfigType.isValid();
	}
	
	private List<String> getConfigLines (String dirPath)
	{
		List<String> lines = new ArrayList<String>();
		int startPos = dirPath.lastIndexOf("/") + 1;
		String appName = dirPath.substring(startPos);
		String filePath = dirPath + "/" +appName+ ".cfg";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		}
		catch (Exception e) { }
		return lines;
	}
}
