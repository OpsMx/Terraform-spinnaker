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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.component.ApplicationStartup;
import com.opsmx.terraspin.interfaces.Provider;
import com.opsmx.terraspin.util.HalConfigUtil;
import com.opsmx.terraspin.util.TerraAppUtil;

@Component
public class TerraService {

	private static final Logger log = LoggerFactory.getLogger(TerraService.class);
	
	ApplicationStartup ApplicationStartup = new ApplicationStartup();
	@Autowired
	TerraAppUtil terraAppUtil;
	
	static String userHomeDir = System.getProperty("user.home");
	static String DEMO_HTML = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title>Opsmx TerraApp</title> </head> <body bgcolor='#000000'> <pre style=\"color:white;\"> \"OPTION_SCPACE\" </pre> </body> </html>";

	@SuppressWarnings("unchecked")
	public String planStart(String payload, String baseURL) {
		String halConfigString = HalConfigUtil.getHalConfig();
		JSONObject halConfigObject = null;
		JSONParser parser = new JSONParser();
		try {
			halConfigObject = (JSONObject) parser.parse(halConfigString);
		} catch (ParseException pe) {
			log.info(":: Exception while parsing halconfig object ::"+halConfigString);
			throw new RuntimeException("Hal config Parse error:", pe);
		}
		JSONObject payloadJsonObject = null;
		try {
			payloadJsonObject = (JSONObject) parser.parse(payload);
		} catch (Exception e) {
			log.info(":: Exception while parsing payload ::"+ payload);
			throw new RuntimeException("Payload parse error ",e);
		}

		String spinApplicationName = (String) payloadJsonObject.get("applicationName");
		String spinPipelineName = (String) payloadJsonObject.get("pipelineName");
		String spinpiPelineId = (String) payloadJsonObject.get("pipelineId");
		String spinPlan = (String) payloadJsonObject.get("plan");
		String spinGitAccount = (String) payloadJsonObject.get("gitAccount");
		String spincloudAccount = (String) payloadJsonObject.get("cloudAccount");
		String applicationName = "applicationName-" + spinApplicationName;
		String pipelineName = "pipelineName-" + spinPipelineName;
		String pipelineId = "pipelineId-" + spinpiPelineId;

		File currentTerraformInfraCodeDir = terraAppUtil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		String statusFilePath = currentTerraformInfraCodeDir + "/planStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));
		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		Map<String, JSONObject> currentCloudProviderObj = terraAppUtil.findProviderObj(halConfigObject,
				spincloudAccount);

		Iterator<Entry<String, JSONObject>> it = currentCloudProviderObj.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) it.next(); // current entry in a loop
			String providerName = (String) entry.getKey();
			JSONObject providerObj = (JSONObject) entry.getValue();
			String fullPathOfProviderImplClass = "com.opsmx.terraApp.interfaces.Provider" + providerName + "Impl";

			try {

				Provider currentProvideObj = (Provider) Class.forName(fullPathOfProviderImplClass).newInstance();
				currentProvideObj.serviceProviderSetting(providerObj, currentTerraformInfraCodeDir);

			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				log.info("Error : cloud provider object creation");
				throw new RuntimeException("cloud provider object creation error ",e);
			} 
		}

		terraServicePlanSetting(halConfigObject, spinGitAccount, spinPlan, spincloudAccount, true,
				currentTerraformInfraCodeDir);

		TerraformPlanThread terraOperationCall = new TerraformPlanThread(currentTerraformInfraCodeDir);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		String statusPollURL = baseURL + "/api/v1/terraform/planStatus/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "RUNNING");
		outRootObj.put("statusurl", statusPollURL);
		log.info("terraform plan status :"+status);
		log.debug("terraform plan status url :"+statusPollURL);
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String planStatus(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/planStatus";
		String planOutputURL = baseURL + "/api/v1/terraform/planOutput/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("RUNNING")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);
				outputJsonObj.put("planOutputURL", planOutputURL);
				log.info("terrafor plan output json :"+outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : parse plan staus");
			throw new RuntimeException("parse plan status error ",e);
		}
		
		return outputJsonObj.toJSONString();
	}

	public String planOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/planStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));

			statusStr = (String) jsonObj.get("output");

		} catch (Exception e) {
			log.info("Error : parse plan out put");
			throw new RuntimeException("parse plan output error ",e);
		}
		String strToR = DEMO_HTML.replaceAll("OPTION_SCPACE", statusStr);
		log.debug("terraform plan out put :"+strToR);
		return strToR;
	}

	@SuppressWarnings("unchecked")
	public String applyStart(String payload, String baseURL) {

		JSONObject payloadJsonObject = null;
		JSONParser parser = new JSONParser();
		try {
			payloadJsonObject = (JSONObject) parser.parse(payload);
		} catch (Exception e) {
			log.info("Exception while parsing payload ");
			throw new RuntimeException("parsing payload in terraform apply error ",e);
		}
		log.debug("terrafor apply Payload :: " + payloadJsonObject + "\n");

		String spinApplicationName = (String) payloadJsonObject.get("applicationName");
		String spinPipelineName = (String) payloadJsonObject.get("pipelineName");
		String spinpiPelineId = (String) payloadJsonObject.get("pipelineId");
		String applicationName = "applicationName-" + spinApplicationName;
		String pipelineName = "pipelineName-" + spinPipelineName;
		String pipelineId = "pipelineId-" + spinpiPelineId;

		String planPath = System.getProperty("user.home") + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName
				+ "/" + pipelineId;

		File planPathDir = new File(planPath);

		String statusFilePath = planPathDir + "/applyStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));

		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		TerraformApplyThread terraOperationCall = new TerraformApplyThread(planPathDir);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		String statusPollURL = baseURL + "/api/v1/terraform/applyStatus/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "RUNNING");
		outRootObj.put("statusurl", statusPollURL);
		log.info("terraform apply status :"+status);
		log.debug("terraform apply status url :"+statusPollURL);
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String applyStatus(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		
		String planPath = System.getProperty("user.home") + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName
				+ "/" + pipelineId;
		
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/applyStatus";

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = new String();
		
		File planPathDir = new File(planPath);
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("running")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);
				
				String applyOutputURL = baseURL + "/api/v1/terraform/applyOutput/" + applicationName + "/" + pipelineName + "/"
						+ pipelineId;

				JSONObject planExeOutputValuesObject = terraServicePlanExeOutputValues(planPathDir, true);

				
				outputJsonObj.put("outputValues", planExeOutputValuesObject);
				outputJsonObj.put("applyOutputURL", applyOutputURL);
				log.info("terraform apply status :"+outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : terraform apply status");
			throw new RuntimeException("terraform apply status error ",e);
		
		}		
		return outputJsonObj.toJSONString();
	}

	public String applyOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {

		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/applyStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("output");
		} catch (Exception e) {
			log.info("Error : terraform apply output");
			throw new RuntimeException("terraform apply output error ",e);
		}
		String strToR = DEMO_HTML.replaceAll("OPTION_SCPACE", statusStr);
		log.debug("terraform apply output :"+strToR);
		return strToR;
	}

	@SuppressWarnings("unchecked")
	public String destroyStart(String payload, String baseURL) {
		
		JSONObject payloadJsonObject = null;
		JSONParser parser = new JSONParser();
		try {
			payloadJsonObject = (JSONObject) parser.parse(payload);
		} catch (Exception e) {
			log.info("Error : terraform destroy paylaod parse");
			throw new RuntimeException("terraform destroy paylaod parse ",e);
		}
		log.info("terraform destroy payload json object :: " + payloadJsonObject + "\n");

		String spinApplicationName = (String) payloadJsonObject.get("applicationName");
		String spinPipelineName = (String) payloadJsonObject.get("pipelineName");
		String spinpiPelineId = (String) payloadJsonObject.get("pipelineId");
		String applicationName = "applicationName-" + spinApplicationName;
		String pipelineName = "pipelineName-" + spinPipelineName;
		String pipelineId = "pipelineId-" + spinpiPelineId;

		String planPath = System.getProperty("user.home") + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName
				+ "/" + pipelineId;

		File planPathDir = new File(planPath);
		
		String statusFilePath = planPathDir + "/destroyStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));
		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);


		TerraformDestroyThread terraOperationCall = new TerraformDestroyThread(planPathDir);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		String statusPollURL = baseURL + "/api/v1/terraform/destroyStatus/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;
		
		JSONObject outRootObj = new JSONObject();
		outRootObj.put("statusurl", statusPollURL);
		outRootObj.put("status", "RUNNING");
		log.info("terraform destroy status :"+status);
		log.debug("terrafor destroy status url :"+statusPollURL);
		return outRootObj.toJSONString();

	}

	@SuppressWarnings("unchecked")
	public String destroyStatus(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/destroyStatus";

		String destroyOutputURL = baseURL + "/api/v1/terraform/destroyOutput/" + applicationName + "/" + pipelineName
				+ "/" + pipelineId;

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("RUNNING")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);
				outputJsonObj.put("destroyOutputURL", destroyOutputURL);
				log.info("terraform destroy status :"+outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : terraform destroy status");
			throw new RuntimeException("Error : terraform destroy status ",e);
		}
       
		return outputJsonObj.toJSONString();
	}

	public String destroyOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {

		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/deleteStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));

			statusStr = (String) jsonObj.get("output");

		} catch (Exception e) {
			log.info("Error : terraform destroy ouput");
			throw new RuntimeException("Error : terraform destroy output ",e);
		}
		String strToR = DEMO_HTML.replaceAll("OPTION_SCPACE", statusStr);
		log.debug("terraform destroy output :"+strToR);
		return strToR;
	}

	@SuppressWarnings("unchecked")
	public JSONObject terraServicePlanExeOutputValues(File terraformCodeDir, boolean isModule) {

		JSONObject planExeOutputValuesJsonObj = new JSONObject();
		Process exec;
		try {

			if (isModule) {
				String exactScriptPath = System.getProperty("user.home") + "/.opsmx/script/exeTerraformGitOutput.sh";
				exec = Runtime.getRuntime().exec(
						new String[] { "/bin/sh", "-c", "sh " + exactScriptPath + " " + terraformCodeDir.getPath() });

			} else {
				String exactScriptPath = System.getProperty("user.home") + "/.opsmx/script/exeTerraformOutput.sh";
				exec = Runtime.getRuntime().exec(
						new String[] { "/bin/sh", "-c", "sh " + exactScriptPath + " " + terraformCodeDir.getPath() });

			}
			exec.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line = "";
			String tempLine = "";
			while ((tempLine = reader.readLine()) != null) {
				String key = tempLine.split("=")[0].trim();
				String value = tempLine.split("=")[1].trim();
				planExeOutputValuesJsonObj.put(key, value);
				line = line + tempLine.trim() + System.lineSeparator();
			}

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = reader2.readLine()) != null) {
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			reader2.close();
			if(!line2.isEmpty())
				log.info("Error : terraform Plan script output values :"+line2);
		} catch (IOException | InterruptedException e) {
			log.info("Error : terraform Plan script OutputValues  ouput");
			throw new RuntimeException("Error : terraform Plan script OutputValues  ouput ",e);
		}
		log.debug("terraform plan output values :"+planExeOutputValuesJsonObj);
		return planExeOutputValuesJsonObj;
	}

	public boolean terraServicePlanSetting(JSONObject halConfigObject, String spinGitAccount, String spinPlan,
			String spincloudAccount, boolean isGitModule, File currentTerraformInfraCodeDir) {
		String terraformInfraCode = null;

		if (StringUtils.isNoneEmpty(spinGitAccount)) {
			String planConfig = new String(
					"module \"terraModule\"{source = \"git::https://GITUSER:GITPASS@github.com/GITUSER/GITPLANURL\"}");
			// String gitPlanUrl = spinPlan.split("https://")[1];
			String gitPlanUrl = spinPlan;
			// JSONObject artifacts = (JSONObject) halConfigObject.get("artifacts");

			JSONArray githubArtifactAccounts = (JSONArray) ((JSONObject) ((JSONObject) halConfigObject.get("artifacts"))
					.get("github")).get("accounts");
			JSONObject githubArtifactAccount = null;

			for (int i = 0; i < githubArtifactAccounts.size(); i++) {
				githubArtifactAccount = (JSONObject) githubArtifactAccounts.get(i);
				String githubArtifactaccountName = (String) githubArtifactAccount.get("name");
				if (StringUtils.equalsIgnoreCase(githubArtifactaccountName.trim(), spincloudAccount.trim()))
					break;
			}
			String gitUser = (String) githubArtifactAccount.get("username");
			String gittoken = (String) githubArtifactAccount.get("token");
			// String gitPass = (String) githubArtifactAccount.get("password");
			isGitModule = true;

			terraformInfraCode = planConfig.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gittoken)
					.replaceAll("GITPLANURL", gitPlanUrl);
		} else {
			terraformInfraCode = spinPlan;
		}

		String infraCodePath = currentTerraformInfraCodeDir.getPath() + "/infraCode.tf";
		File infraCodfile = new File(infraCodePath);
		if (!infraCodfile.exists()) {
			try {
				infraCodfile.createNewFile();
			} catch (IOException e) {
				log.info("Error : terraform InfrCodfile Creation");
				throw new RuntimeException("Error : terraform InfrCodfile Creation ",e);

			}
		}

		InputStream infraCodeInputStream = new ByteArrayInputStream(
				terraformInfraCode.getBytes(StandardCharsets.UTF_8));
		terraAppUtil.overWriteStreamOnFile(infraCodfile, infraCodeInputStream);

		return isGitModule;
	}	

}
