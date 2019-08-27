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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TerraAppUtil {

	private static final Logger log = LoggerFactory.getLogger(TerraAppUtil.class);

	static final String separator = File.separator;

	public File createDirForPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = applicaton;
		String PipelineName = pipeline;
		String PipelineId = pipelineId;

		String currentUserDir = System.getProperty("user.home");
		String opsmxdir = currentUserDir + separator + ".opsmx";
		log.debug(" opsmx directror :" + opsmxdir);
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File spinnakerDirFile = new File(opsmxDirFile.getPath() + separator + "spinnaker");
		if (!spinnakerDirFile.exists())
			spinnakerDirFile.mkdir();

		File applicationDirFile = new File(spinnakerDirFile.getPath() + separator + ApplicationName);
		if (!applicationDirFile.exists())
			applicationDirFile.mkdir();

		File pipelineNameDirFile = new File(applicationDirFile.getPath() + separator + PipelineName);
		if (!pipelineNameDirFile.exists())
			pipelineNameDirFile.mkdir();

		File pipelineIdDirFile = new File(pipelineNameDirFile.getPath() + separator + PipelineId);
		if (!pipelineIdDirFile.exists())
			pipelineIdDirFile.mkdir();
		log.debug(" Succesfully created the pipeline directory :" + pipelineIdDirFile.getPath());
		return pipelineIdDirFile;
	}

	public String getDirPathOfPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = "applicationName-" + applicaton;
		String PipelineName = "pipelineName-" + pipeline;
		String PipelineIdName = "pipelineId-" + pipelineId;
		String currentUserDir = System.getProperty("user.home");
		String pipelineIdDir = currentUserDir + separator + ".opsmx" + separator + "spinnaker" + separator
				+ ApplicationName + separator + PipelineName + separator + PipelineIdName;
		log.debug(" pipeline directory :" + pipelineIdDir);
		return pipelineIdDir;
	}

	public void writeStreamOnFile(File file, InputStream stream) {

		boolean append = true;
		boolean autoFlush = true;
		String charset = "UTF-8";

		FileOutputStream fos;
		OutputStreamWriter osw;
		try {
			fos = new FileOutputStream(file, append);
			osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw, autoFlush);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				pw.write(inputLine);
				pw.write("\n");
			}

			in.close();
			pw.close();
			log.info("completed writing in to file :" + file.getPath());
		} catch (Exception e) {
			log.info("Error : resource stream writing ");
			throw new RuntimeException("Error : resource stream writing ", e);
		}
	}

	public void overWriteStreamOnFile(File file, InputStream stream) {

		boolean append = false;
		boolean autoFlush = true;
		String charset = "UTF-8";

		FileOutputStream fos;
		OutputStreamWriter osw;
		try {
			fos = new FileOutputStream(file, append);
			osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw, autoFlush);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				pw.write(inputLine);
				pw.write("\n");
			}
			in.close();
			pw.close();

			log.info("completed writing in to file :" + file.getPath());
		} catch (Exception e) {
			log.info("Error : resource stream over writing ");
			throw new RuntimeException("Error : resource stream over writing ", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, JSONObject> findProviderObj(JSONObject halConfigObject, String spincloudAccount) {

		log.info("In TerraAppUtil find provider object method :::: \n");
		LinkedList<String> providerList = new LinkedList<String>();
		providerList.add("dcos");
		providerList.add("kubernetes");
		providerList.add("aws");
		providerList.add("google");
		providerList.add("oracle");
		providerList.add("appengine");
		providerList.add("ecs");
		providerList.add("cloudfoundry");
		providerList.add("azure");

		Map<String, JSONObject> rootMapObj = new HashMap<String, JSONObject>();
		JSONObject actualCloudProviderObj = null;

		JSONObject cloudProviders = (JSONObject) halConfigObject.get("providers");
		log.debug("Configured cloud providers : " + cloudProviders.toJSONString());
		for (int i = 0; i < providerList.size(); i++) {

			boolean isFound = false;
			JSONObject currentProvider = null;
			String providerName = providerList.get(i);
			currentProvider = (JSONObject) cloudProviders.get(providerName);
			JSONArray currentProviderAccounts = null;
			currentProviderAccounts = (JSONArray) currentProvider.get("accounts");

			if (currentProviderAccounts.isEmpty())
				continue;
			else {
				for (int j = 0; j < currentProviderAccounts.size(); j++) {

					JSONObject currentProviderAccount = null;
					currentProviderAccount = (JSONObject) currentProviderAccounts.get(j);
					String currentProviderAccountName = (String) currentProviderAccount.get("name");
					if (StringUtils.equalsIgnoreCase(currentProviderAccountName.trim(), spincloudAccount.trim())) {
						actualCloudProviderObj = currentProviderAccount;
						isFound = true;
						break;
					} else {
						continue;
					}
				}
			}

			if (isFound) {
				if (StringUtils.equalsIgnoreCase(providerName.trim(), "aws")) {
					actualCloudProviderObj.put("accessKeyId", currentProvider.get("accessKeyId"));
					actualCloudProviderObj.put("secretAccessKey", currentProvider.get("secretAccessKey"));
				}
				rootMapObj.put(providerName, actualCloudProviderObj);
				break;
			}

		}
		log.info("actual cloud provider : " + rootMapObj);
		return rootMapObj;
	}

	public static void main(String... args) {}
}
