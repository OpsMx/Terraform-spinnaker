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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	//private static final String CONFIG_FILE = "/home/opsmx/lalit/work/opsmx/Terraform-spinnaker/TerraSpin/container/application.properties"; 
	private static final String CONFIG_FILE = "/home/terraspin/opsmx/app/config/application.properties"; 
	private static Properties properties; // Singleton instance
	private PropertiesUtil() {}  // Dont instantiate a singleton class

	public synchronized static Properties getInstance(){
		properties = new Properties();
		loadProps();
		return properties;
	}

	private static void loadProps() {
		try(InputStream in = new FileInputStream(CONFIG_FILE);) {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
