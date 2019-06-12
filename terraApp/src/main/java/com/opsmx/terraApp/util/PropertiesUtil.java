package com.opsmx.terraApp.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesUtil {

	private static Properties properties; // Singleton instance
	
	public synchronized static Properties getInstance(){
		if(properties == null){
			properties = new Properties();
			loadProps();
		}
		return properties;
	}

	private static void loadProps() {
		String propFile = System.getProperty("user.dir") + "/application.properties";
		try{
			InputStream inputStream = new ByteArrayInputStream(propFile.getBytes(StandardCharsets.UTF_8));
		    //load the file handle for main.properties
		    FileInputStream file = new FileInputStream(propFile);
			properties.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
