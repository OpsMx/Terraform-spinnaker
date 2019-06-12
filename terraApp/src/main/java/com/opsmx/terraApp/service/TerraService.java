/**
 * 
 */
package com.opsmx.terraApp.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opsmx.terraApp.component.ApplicationStartup;
import com.opsmx.terraApp.util.HalConfigUtil;
import com.opsmx.terraApp.util.TerraAppUtil;

/**
 * @author Lalit
 *
 */

@Service
public class TerraService {

	@Autowired
	TerraAppUtil terraAppUtil = new TerraAppUtil();
	ApplicationStartup ApplicationStartup = new ApplicationStartup();
	JSONParser parser = new JSONParser();
	static String DEMO_HTML = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title>Opsmx TerraApp</title> </head> <body bgcolor='#000000'> <pre style=\"color:white;\"> \"OPTION_SCPACE\" </pre> </body> </html>";

	@SuppressWarnings("unchecked")
	public String planStart(String payload, String baseURL) {

		String halConfigString = HalConfigUtil.getHalConfig();
		JSONObject halConfigObject = null;
		try {
			halConfigObject = (JSONObject) parser.parse(halConfigString);
		} catch (ParseException e1) {
			System.out.println("Exception while parsing halconfig object");
			e1.printStackTrace();
		}
		JSONObject payloadJsonObject = null;
		try {
			payloadJsonObject = (JSONObject) parser.parse(payload);
		} catch (Exception e) {
			System.out.println("Exception while parsing payload");
			e.printStackTrace();
		}

		System.out.println("::::  In terraform start plan services :::: \n");
		System.out.println("Payload json object :: " + payloadJsonObject + "\n");
		System.out.println("Hal config object :: " + halConfigObject + "\n");

		String spinApplicationName = (String) payloadJsonObject.get("applicationName");
		String spinPipelineName = (String) payloadJsonObject.get("pipelineName");
		String spinpiPelineId = (String) payloadJsonObject.get("pipelineId");
		String spinPlan = (String) payloadJsonObject.get("plan");
		String spinGitAccount = (String) payloadJsonObject.get("gitAccount");
		String spincloudAccount = (String) payloadJsonObject.get("cloudAccount");
		String spincloudKind = (String) payloadJsonObject.get("cloudKind");
		String applicationName = "applicationName-" + spinApplicationName;
		String pipelineName = "pipelineName-" + spinPipelineName;
		String pipelineId = "pipelineId-" + spinpiPelineId;
		boolean isGitModule = false;

		File currentTerraformInfraCodeDir = terraAppUtil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);
		System.out.println("terraform plan Dir :: " + currentTerraformInfraCodeDir.getPath() + "\n");
		terraServiceProviderSetting(halConfigObject, spincloudKind, spincloudAccount, currentTerraformInfraCodeDir);
		isGitModule = terraServicePlanSetting(halConfigObject, spinGitAccount, spinPlan, spincloudAccount, isGitModule,
				currentTerraformInfraCodeDir);

		// Call terraform plan
		// //////////////////////////////////////////////////////////

		System.out.println("Exe thread start time ::::::::::   " + System.currentTimeMillis());
		TerraformServiceThread terraOperationCall = new TerraformServiceThread(currentTerraformInfraCodeDir);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		try {
			trigger.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Exe thread end time ::::::::::   " + System.currentTimeMillis());

		String statusPollURL = baseURL + "/api/v1/terraform/planStatus/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;
		String planOutputURL = baseURL + "/api/v1/terraform/planOutput/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject planExeOutputValuesObject = terraServicePlanExeOutputValues(currentTerraformInfraCodeDir,
				isGitModule);
		System.out.println("Exe plan output values :: " + planExeOutputValuesObject);

		JSONObject outRootObj = new JSONObject();
//		outRootObj.put("status", "true");
//		outRootObj.put("statusurl", statusPollURL);
		outRootObj.put("planOutputURL", planOutputURL);
		outRootObj.put("outputValues", planExeOutputValuesObject);

		return outRootObj.toJSONString();
	}

	public void executeTerraformCmd() {
		System.out.println("Executing terraform command");
	}

	@SuppressWarnings("unchecked")
	public String planStatus(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		String currentSatusDir = System.getProperty("user.home") + "/.opsmx/spinnaker/" + applicationName + "/"
				+ pipelineName + "/" + pipelineId + "/status";
		System.out.println("In terraform status current file path :: " + currentSatusDir);
		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("running")) {
				outputJsonObj.put("status", "running");
				// outputJsonObj.put("progress", "running");
			}
			if (statusStr.equalsIgnoreCase("true")) {
				outputJsonObj.put("status", "true");
				// outputJsonObj.put("progress", "completed");
			}
			if (statusStr.equalsIgnoreCase("false")) {
				outputJsonObj.put("status", "false");
				// outputJsonObj.put("progress", "terminated");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * String pollURL = baseURL + "/api/statusTerraformU/" + payload;
		 * outputJsonObj.put("unifiedLogURL", pollURL); outputJsonObj.put("progress",
		 * pollURL); outputJsonObj.put("Code", pollURL); outputJsonObj.put("Response",
		 * pollURL);
		 */

		System.out.println("In terraform status API block current status :: " + outputJsonObj.toJSONString());
		return outputJsonObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String planOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		// String currentSatusDir = System.getProperty("user.home") + "/tempCurrent/" +
		// payload + "/status";
		String currentSatusDir = System.getProperty("user.home") + "/.opsmx/spinnaker/" + applicationName + "/"
				+ pipelineName + "/" + pipelineId + "/status";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("output");

			outputJsonObj.put("UnifiedLog", statusStr);

		} catch (Exception e) {
			e.printStackTrace();
		}

		outputJsonObj.put("progress", "completed");
		System.out.println(
				"In terraform Log unifiedstatus API block current status :: " + outputJsonObj.toJSONString() + "\n");
		String strToR = DEMO_HTML.replaceAll("OPTION_SCPACE", statusStr);
		System.out.println("In terraform Log strToR :: " + strToR + "\n");
		// return outputJsonObj.toJSONString();
		return strToR;
	}

	@SuppressWarnings("unchecked")
	public String planDelete(String payload, String baseURL) {

		System.out.println("::::  In terraform delete plan services :::: \n");
		JSONObject payloadJsonObject = null;
		try {
			payloadJsonObject = (JSONObject) parser.parse(payload);
		} catch (Exception e) {
			System.out.println("Exception while parsing payload");
			e.printStackTrace();
		}
		System.out.println("Payload json object :: " + payloadJsonObject + "\n");

		String spinApplicationName = (String) payloadJsonObject.get("applicationName");
		String spinPipelineName = (String) payloadJsonObject.get("pipelineName");
		String spinpiPelineId = (String) payloadJsonObject.get("pipelineId");
		String applicationName = "applicationName-" + spinApplicationName;
		String pipelineName = "pipelineName-" + spinPipelineName;
		String pipelineId = "pipelineId-" + spinpiPelineId;

		String planPath = System.getProperty("user.home") + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName
				+ "/" + pipelineId;
		File planPathDir = new File(planPath);
		if (planPathDir.exists()) {
			try {
				FileUtils.deleteDirectory(planPathDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "True");
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject terraServicePlanExeOutputValues(File terraformCodeDir, boolean isModule) {

		System.out.println("::::  In plan Exe Output Values method :::: \n");
		System.out.println("isModule :: " + isModule);
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
				// line = line.concat("/\n" + " ***** :: " + tempLine.trim());
				System.out.println("temp output line :: " + tempLine);
				String key = tempLine.split("=")[0].trim();
				String value = tempLine.split("=")[1].trim();
				planExeOutputValuesJsonObj.put(key, value);
				line = line + tempLine.trim() + System.lineSeparator();
			}

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = reader2.readLine()) != null) {
				// line2 = line2.concat("/\n" + " ***** :: " +tempLine2.trim());
				// terraServicePlanSetting
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			reader2.close();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return planExeOutputValuesJsonObj;
	}

	public void terraServiceProviderSetting(JSONObject halConfigObject, String spincloudKind, String spincloudAccount,
			File currentTerraformInfraCodeDir) {
		// cloud provider setting for terraform plan
		// //////////////////////////////////////////////////////////

		System.out.println("::::  In terraform Service Provider Setting method :::: \n");
		JSONObject cloudProviders = (JSONObject) halConfigObject.get("providers");
		JSONObject currentSpincloudKind = (JSONObject) cloudProviders.get(spincloudKind);

		System.out.println(
				"cloud providr :: " + cloudProviders + " \n and current cloud kind" + currentSpincloudKind + " \n ");
		JSONArray cloudKindObjects = (JSONArray) ((JSONObject) ((JSONObject) halConfigObject.get("providers"))
				.get(spincloudKind)).get("accounts");
		JSONObject currentCloudKind = null;

		for (int i = 0; i < cloudKindObjects.size(); i++) {
			currentCloudKind = (JSONObject) cloudKindObjects.get(i);
			String accountName = (String) currentCloudKind.get("name");
			if (StringUtils.equalsIgnoreCase(accountName.trim(), spincloudAccount.trim()))
				break;
		}

		String kubeconfigFile = (String) currentCloudKind.get("kubeconfigFile");
		String providerConfig = "provider \"kubernetes\"" + "{ config_path = \"" + kubeconfigFile + "\" }";

		String provideFilePath = currentTerraformInfraCodeDir.getPath() + "/provider.tf";
		File providerFile = new File(provideFilePath);
		if (!providerFile.exists()) {
			try {
				providerFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		InputStream providerCodeInputStream = new ByteArrayInputStream(providerConfig.getBytes(StandardCharsets.UTF_8));
		terraAppUtil.overWriteStreamOnFile(providerFile, providerCodeInputStream);

	}

	public boolean terraServicePlanSetting(JSONObject halConfigObject, String spinGitAccount, String spinPlan,
			String spincloudAccount, boolean isGitModule, File currentTerraformInfraCodeDir) {
		// Infra code setting for terraform plan
		// //////////////////////////////////////////////////////////
		// module "moduletest" {source =
		// "git::https://lalitv92:31f40c909efea80a33cbb953666dd25030ed52d2@github.com/lalitv92/terraformtest.git//Infra"}

		System.out.println("::::  In terraform Service Plan Setting method :::: \n");

		String terraformInfraCode = null;

		if (StringUtils.isNoneEmpty(spinGitAccount)) {
			String planConfig = new String(
					"module \"terraModule\"{source = \"git::https://GITUSER:GITPASS@GITPLANURL\"}");
			String gitPlanUrl = spinPlan.split("https://")[1];
			JSONObject artifacts = (JSONObject) halConfigObject.get("artifacts");

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		InputStream infraCodeInputStream = new ByteArrayInputStream(
				terraformInfraCode.getBytes(StandardCharsets.UTF_8));
		terraAppUtil.overWriteStreamOnFile(infraCodfile, infraCodeInputStream);
		return isGitModule;

	}

	public static void main(String... args) {

		String gitPlan = "https://github.com/lalitv92/terraformtest.git//Infra".split("https://")[1];
		System.out.println("aaab :: " + gitPlan);
		// TerraService tu = new TerraService();
		// tu.planStart("dssa", "aaa");
		// System.out.println(tu.createDirForPipelineId("test","deploy","excg4587ut"));
	}

}
