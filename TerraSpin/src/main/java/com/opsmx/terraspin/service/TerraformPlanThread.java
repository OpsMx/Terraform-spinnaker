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

package com.opsmx.terraspin.service;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opsmx.terraspin.util.TerraAppUtil;

class TerraformPlanThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TerraformPlanThread.class);
	
	private File file;	
	
	public TerraformPlanThread() {
		
	}

	public TerraformPlanThread(File file) {

		this.file = file;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		String planScriptPath = System.getProperty("user.home") + "/.opsmx/script/exeTerraformPlan.sh";
		log.info("terraform plan script path : "+planScriptPath);
		TerraAppUtil terraAppUtil = new TerraAppUtil();
		Process exec;
		try {
			exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c",
					"printf 'yes' | sh " + planScriptPath + " " + file.getPath() });
			exec.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line = "";
			String tempLine = "";
			while ((tempLine = reader.readLine()) != null) {				
				line = line + tempLine.trim() + System.lineSeparator();
			}

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = reader2.readLine()) != null) {				
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			reader2.close();

			JSONObject statusRootObj = new JSONObject();
			if (exec.exitValue() == 0) {
				statusRootObj.put("status", "SUCCESS");
				statusRootObj.put("output", line);
			} else {
				statusRootObj.put("status", "TERMINAL");
				statusRootObj.put("output", line2);
				log.info("terraform plan script error stream : "+line2);
			}

			String filePath = file.getPath() + "/planStatus";
			File statusFile = new File(filePath);
			InputStream statusInputStream = new ByteArrayInputStream(
					statusRootObj.toString().getBytes(StandardCharsets.UTF_8));
			terraAppUtil.overWriteStreamOnFile(statusFile, statusInputStream);
			log.debug("terraform plan execution status :"+statusRootObj);
		} catch (IOException | InterruptedException e) {
			log.info("terraform plan execution execption message :"+e.getMessage());			
		    throw new RuntimeException("terraform plan execution",e);
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}