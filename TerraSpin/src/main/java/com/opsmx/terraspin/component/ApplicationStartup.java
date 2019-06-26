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

package com.opsmx.terraspin.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.util.HalConfigUtil;
import com.opsmx.terraspin.util.TerraAppUtil;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	/**
	 * This method is called during Spring's startup.
	 * 
	 * @param event Event raised when an ApplicationContext gets initialized or
	 *              refreshed.
	 */
	private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);
	static final String separator = File.separator;
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		
		TerraAppUtil tu = new TerraAppUtil();
		// logic to copy script in-place
		String currentUserDir = System.getProperty("user.home");
        log.info("terraform user directory :"+currentUserDir);
		String opsmxdir = currentUserDir + separator+".opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File scriptDirFile = new File(opsmxDirFile.getPath() + separator+ "script");
		if (!scriptDirFile.exists())
			scriptDirFile.mkdir();

		File terraformApplySource = new File(scriptDirFile.getPath() +separator + "exeTerraformApply.sh");
		tu.overWriteStreamOnFile(terraformApplySource,
				getClass().getClassLoader().getResourceAsStream(separator+"script"+separator+"exeTerraformApply.sh"));

		File terraformPlanSource = new File(scriptDirFile.getPath()+separator + "exeTerraformPlan.sh");
		tu.overWriteStreamOnFile(terraformPlanSource,
				getClass().getClassLoader().getResourceAsStream(separator+"script"+separator+"exeTerraformPlan.sh"));

		File terraformOutputSource = new File(scriptDirFile.getPath()+separator + "exeTerraformOutput.sh");
		tu.overWriteStreamOnFile(terraformOutputSource,
				getClass().getClassLoader().getResourceAsStream(separator+"script"+separator+"exeTerraformOutput.sh"));

		File terraformGitOutputSource = new File(scriptDirFile.getPath() +separator + "exeTerraformGitOutput.sh");
		tu.overWriteStreamOnFile(terraformGitOutputSource,
				getClass().getClassLoader().getResourceAsStream(separator+"script"+separator+"exeTerraformGitOutput.sh"));

		File terraformDestroySource = new File(scriptDirFile.getPath() +separator + "exeTerraformDestroy.sh");
		tu.overWriteStreamOnFile(terraformDestroySource,
				getClass().getClassLoader().getResourceAsStream(separator+"script"+separator+"exeTerraformDestroy.sh"));

		File halConfigSource = new File(scriptDirFile.getPath() +separator + "exeHalConfig.sh");
		tu.overWriteStreamOnFile(halConfigSource,
				getClass().getClassLoader().getResourceAsStream(separator+"script"+separator+"exeHalConfig.sh"));
		
		HalConfigUtil.setHalConfig(halConfig(halConfigSource));
		
	}

	@SuppressWarnings("unchecked")
	public String halConfig(File file) {
		log.info("Hal config script path : " + file.getPath());
		JSONObject halConfigRootObj = new JSONObject();
		Process exec;
		try {
			exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "sh " + file.getPath() });
			exec.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line = "";
			String tempLine = "";
			while ((tempLine = reader.readLine()) != null) {
				line = line + tempLine.trim() + System.lineSeparator();
			}

			BufferedReader errorReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = errorReader.readLine()) != null) {
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			errorReader.close();

			if (exec.exitValue() == 0) {
				int startIndex = line.indexOf('{');
				String halConfigString = line.substring(startIndex);
				JSONParser parser = new JSONParser();
				halConfigRootObj = (JSONObject) parser.parse(halConfigString);
				log.info("Successfully parsed hal config ");

			} else {
				halConfigRootObj.put("error", line2);
				log.info("Error while fetching hal config please make sure you hal daemaon is running");
			}

		} catch (IOException | InterruptedException | ParseException e) {
			log.info("Malformed Hal config Error :"+e.getMessage()); 
			throw new RuntimeException("Malformed Hal config data", e);
		}
		
		return halConfigRootObj.toJSONString();
	}

}