/*
 * Copyright 2019 OpsMX, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.terraspin.util;

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
			@SuppressWarnings("unused")
			InputStream inputStream = new ByteArrayInputStream(propFile.getBytes(StandardCharsets.UTF_8));
		    FileInputStream file = new FileInputStream(propFile);
			properties.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
