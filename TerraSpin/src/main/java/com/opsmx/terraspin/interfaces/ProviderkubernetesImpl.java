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
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opsmx.terraspin.util.PropertiesUtil;
import com.opsmx.terraspin.util.TerraAppUtil;

public class ProviderkubernetesImpl implements Provider {
	
	private static final Logger log = LoggerFactory.getLogger(ProviderkubernetesImpl.class);
	Properties properties = PropertiesUtil.getInstance();
	
	private String isContainer = properties.getProperty("application.iscontainer.env");
	
	@Override
	public void serviceProviderSetting(JSONObject cloudProvideObj, File currentTerraformInfraCodeDir) {

		log.info("::::  In serviceProviderSetting method of ProviderkubernetesImpl class :::: \n");
		TerraAppUtil terraAppUtil = new TerraAppUtil();
		log.debug("::::  In serviceProviderSetting cloud obj :::: \n" + cloudProvideObj);
		String kubeconfigFile = new String();
		String kubeAccountName = (String) cloudProvideObj.get("name");
		
		log.info("::::  In ProviderkubernetesImpl class iscontainer env::"+ isContainer +" kube account name :: "+ kubeAccountName +"\n" );
		if(StringUtils.equalsAnyIgnoreCase("true", isContainer)){
			kubeconfigFile = "/home/terraspin/opsmx/kubeaccount/"+kubeAccountName.trim();
			//kubeconfigFile = "/home/opsmx/lalit/work/opsmx/Terraform-spinnaker/TerraSpin/container/"+kubeAccountName.trim();
		}else {
			kubeconfigFile = (String) cloudProvideObj.get("kubeconfigFile");
		}
		
		String providerConfig = "provider \"kubernetes\"" + "{ config_path = \"" + kubeconfigFile + "\" }";
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
		
		log.info("kube path :: "+kubeconfigFile+"\n:: Input stream :: \n" + providerCodeInputStream +" \n  file object ::"+providerFile);
        
		terraAppUtil.overWriteStreamOnFile(providerFile, providerCodeInputStream);
	}

}
