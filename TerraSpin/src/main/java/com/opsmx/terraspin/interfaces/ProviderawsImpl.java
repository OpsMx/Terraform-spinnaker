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

package com.opsmx.terraspin.interfaces;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opsmx.terraspin.util.TerraAppUtil;

public class ProviderawsImpl implements Provider {
	
	private static final Logger log = LoggerFactory.getLogger(ProviderawsImpl.class);	
	
	@Override
	public void serviceProviderSetting(JSONObject cloudProvideObj, File currentTerraformInfraCodeDir) {

		log.info("::::  In serviceProviderSetting method of ProviderawsImpl class :::: \n");
		TerraAppUtil terraAppUtil = new TerraAppUtil();
		log.debug("::::  In AWS service provider setting cloud obj :::: \n" + cloudProvideObj);
		JSONArray regionArrayObj = (JSONArray) cloudProvideObj.get("regions");
		JSONObject regionObj = (JSONObject) regionArrayObj.get(0);
		
		String MYREGION = (String) regionObj.get("name");
		String MYACCESSKEY = (String) cloudProvideObj.get("accessKeyId");
		String MYSECRETKEY = (String) cloudProvideObj.get("secretAccessKey");
		
		String providerConfig = "provider \"aws\"" + "{ region = \"" + MYREGION + "\"" + "\n access_key = \"" + MYACCESSKEY + "\"" + "\n secret_key = \"" + MYSECRETKEY + "\"  }";
		String provideFilePath = currentTerraformInfraCodeDir.getPath() + "/provider.tf";
		log.info(" Provider file path :"+provideFilePath);
		File providerFile = new File(provideFilePath);
		if (!providerFile.exists()) {
			try {
				providerFile.createNewFile();
			} catch (IOException e) {
				log.info("Error creating provider file :"+e.getMessage());
				 throw new RuntimeException("Error in provide file creation", e);			
			}
		}

		InputStream providerCodeInputStream = new ByteArrayInputStream(providerConfig.getBytes(StandardCharsets.UTF_8));
		terraAppUtil.overWriteStreamOnFile(providerFile, providerCodeInputStream);
	}

}
