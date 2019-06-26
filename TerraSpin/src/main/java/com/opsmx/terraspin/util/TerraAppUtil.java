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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
		String opsmxdir = currentUserDir +separator+ ".opsmx";
		log.debug(" opsmx directror :"+opsmxdir);
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File spinnakerDirFile = new File(opsmxDirFile.getPath() + separator + "spinnaker");
		if (!spinnakerDirFile.exists())
			spinnakerDirFile.mkdir();

		File applicationDirFile = new File(spinnakerDirFile.getPath() + separator + ApplicationName);
		if (!applicationDirFile.exists())
			applicationDirFile.mkdir();

		File pipelineNameDirFile = new File(applicationDirFile.getPath() + separator+ PipelineName);
		if (!pipelineNameDirFile.exists())
			pipelineNameDirFile.mkdir();

		File pipelineIdDirFile = new File(pipelineNameDirFile.getPath() + separator + PipelineId);
		if (!pipelineIdDirFile.exists())
			pipelineIdDirFile.mkdir();		
		log.debug(" Succesfully created the pipeline directory :"+pipelineIdDirFile.getPath());
	    return pipelineIdDirFile;
	}

	public String getDirPathOfPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = "applicationName-" + applicaton;
		String PipelineName = "pipelineName-" + pipeline;
		String PipelineIdName = "pipelineId-" + pipelineId;
		String currentUserDir = System.getProperty("user.home");
		String pipelineIdDir = currentUserDir +separator+".opsmx"+separator+"spinnaker"+separator + ApplicationName + separator + PipelineName + separator
				+ PipelineIdName;
		log.debug(" pipeline directory :"+pipelineIdDir);
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
          log.info("completed writing in to file :"+file.getPath());
		} catch (Exception e) {
			log.info("Error : resource stream writing ");
			throw new RuntimeException("Error : resource stream writing ",e);
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
			
			log.info("completed writing in to file :"+file.getPath());
		} catch (Exception e) {
			log.info("Error : resource stream over writing ");
			throw new RuntimeException("Error : resource stream over writing ", e);
		}
	}

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
		log.debug("Configured cloud providers : "+cloudProviders.toJSONString());
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
				rootMapObj.put(providerName, actualCloudProviderObj);
				break;
			}
			
		}
		log.info("actual cloud provider : "+rootMapObj);
		return rootMapObj;
	}

	public static void main(String... args) {
		 TerraAppUtil tu = new TerraAppUtil();
		// tu.createDirForPipelineId("test", "depl2oy", "excg4587ut");
		// System.out.println("Properties " + System.getProperties().toString());
		// System.out.println(tu.createDirForPipelineId("test", "deploy",
		// "excg4587ut"));

		String test = "{\n" + "  \"providers\": {\n" + "    \"appengine\": {\n" + "      \"accounts\": [],\n"
				+ "      \"enabled\": false\n" + "    },\n" + "    \"kubernetes\": {\n" + "      \"accounts\": [\n"
				+ "        {\n" + "          \"providerVersion\": \"V2\",\n" + "          \"omitKinds\": [\n"
				+ "            \"podPreset\"\n" + "          ],\n" + "          \"omitNamespaces\": [],\n"
				+ "          \"kinds\": [],\n" + "          \"customResources\": [],\n"
				+ "          \"oauthScopes\": [],\n" + "          \"dockerRegistries\": [],\n"
				+ "          \"checkPermissionsOnStartup\": false,\n" + "          \"liveManifestCalls\": true,\n"
				+ "          \"kubeconfigFile\": \"/home/opsmxgcetest/.kube/config\",\n"
				+ "          \"onlySpinnakerManaged\": false,\n" + "          \"permissions\": {},\n"
				+ "          \"name\": \"openshift1-account\",\n"
				+ "          \"context\": \"default/35-237-183-196:8443/CN=sagnik,CN=Users,DC=local,DC=opsmx,DC=com\",\n"
				+ "          \"configureImagePullSecrets\": true,\n" + "          \"cacheThreads\": 1,\n"
				+ "          \"oAuthScopes\": [],\n" + "          \"cachingPolicies\": [],\n"
				+ "          \"requiredGroupMembership\": [],\n" + "          \"namespaces\": [\n"
				+ "            \"default\",\n" + "            \"arihant\"\n" + "          ]\n" + "        },\n"
				+ "        {\n" + "          \"providerVersion\": \"V1\",\n" + "          \"omitKinds\": [],\n"
				+ "          \"omitNamespaces\": [],\n" + "          \"kinds\": [],\n"
				+ "          \"customResources\": [],\n" + "          \"oauthScopes\": [],\n"
				+ "          \"dockerRegistries\": [\n" + "            {\n"
				+ "              \"accountName\": \"my-docker-registry\",\n" + "              \"namespaces\": []\n"
				+ "            }\n" + "          ],\n"
				+ "          \"kubeconfigFile\": \"/home/opsmxgcetest/.kube/config\",\n"
				+ "          \"onlySpinnakerManaged\": false,\n" + "          \"permissions\": {},\n"
				+ "          \"name\": \"my-k8s-account\",\n" + "          \"configureImagePullSecrets\": true,\n"
				+ "          \"cacheThreads\": 1,\n" + "          \"oAuthScopes\": [],\n"
				+ "          \"cachingPolicies\": [],\n" + "          \"requiredGroupMembership\": [],\n"
				+ "          \"namespaces\": []\n" + "        },\n" + "        {\n"
				+ "          \"providerVersion\": \"V2\",\n" + "          \"omitKinds\": [],\n"
				+ "          \"omitNamespaces\": [],\n" + "          \"kinds\": [],\n"
				+ "          \"customResources\": [],\n" + "          \"oauthScopes\": [],\n"
				+ "          \"dockerRegistries\": [],\n"
				+ "          \"kubeconfigFile\": \"/home/opsmxgcetest/.kube/config\",\n"
				+ "          \"onlySpinnakerManaged\": false,\n" + "          \"permissions\": {},\n"
				+ "          \"name\": \"my-k8s-v2-account\",\n"
				+ "          \"context\": \"gke_my-orbit-project-71824_us-central1-a_gke-standard-cluster\",\n"
				+ "          \"configureImagePullSecrets\": true,\n" + "          \"cacheThreads\": 1,\n"
				+ "          \"oAuthScopes\": [],\n" + "          \"cachingPolicies\": [],\n"
				+ "          \"requiredGroupMembership\": [],\n" + "          \"namespaces\": []\n" + "        },\n"
				+ "        {\n" + "          \"providerVersion\": \"V2\",\n" + "          \"omitKinds\": [],\n"
				+ "          \"omitNamespaces\": [],\n" + "          \"kinds\": [],\n"
				+ "          \"customResources\": [],\n" + "          \"oauthScopes\": [],\n"
				+ "          \"dockerRegistries\": [],\n"
				+ "          \"kubeconfigFile\": \"/home/opsmxgcetest/.kube/devk8slocal\",\n"
				+ "          \"onlySpinnakerManaged\": false,\n" + "          \"permissions\": {},\n"
				+ "          \"name\": \"devk8s-v2local\",\n" + "          \"configureImagePullSecrets\": true,\n"
				+ "          \"cacheThreads\": 1,\n" + "          \"oAuthScopes\": [],\n"
				+ "          \"cachingPolicies\": [],\n" + "          \"requiredGroupMembership\": [],\n"
				+ "          \"namespaces\": []\n" + "        },\n" + "        {\n"
				+ "          \"providerVersion\": \"V2\",\n" + "          \"omitKinds\": [],\n"
				+ "          \"omitNamespaces\": [],\n" + "          \"kinds\": [],\n"
				+ "          \"customResources\": [],\n" + "          \"oauthScopes\": [],\n"
				+ "          \"dockerRegistries\": [],\n"
				+ "          \"kubeconfigFile\": \"/home/opsmxgcetest/.kube/eksconfig\",\n"
				+ "          \"onlySpinnakerManaged\": false,\n" + "          \"permissions\": {},\n"
				+ "          \"name\": \"spin-v2-ekscluster\",\n" + "          \"context\": \"aws\",\n"
				+ "          \"configureImagePullSecrets\": true,\n" + "          \"cacheThreads\": 1,\n"
				+ "          \"oAuthScopes\": [],\n" + "          \"cachingPolicies\": [],\n"
				+ "          \"requiredGroupMembership\": [],\n" + "          \"namespaces\": []\n" + "        }\n"
				+ "      ],\n" + "      \"enabled\": true,\n" + "      \"primaryAccount\": \"openshift1-account\"\n"
				+ "    },\n" + "    \"oracle\": {\n" + "      \"bakeryDefaults\": {\n"
				+ "        \"templateFile\": \"oci.json\",\n" + "        \"baseImages\": []\n" + "      },\n"
				+ "      \"accounts\": [],\n" + "      \"enabled\": false\n" + "    },\n" + "    \"ecs\": {\n"
				+ "      \"accounts\": [\n" + "        {\n" + "          \"providerVersion\": \"V1\",\n"
				+ "          \"permissions\": {},\n" + "          \"awsAccount\": \"ec2account\",\n"
				+ "          \"name\": \"ecs-spinnaker\",\n" + "          \"requiredGroupMembership\": []\n"
				+ "        }\n" + "      ],\n" + "      \"enabled\": false,\n"
				+ "      \"primaryAccount\": \"ecs-spinnaker\"\n" + "    },\n" + "    \"dockerRegistry\": {\n"
				+ "      \"accounts\": [\n" + "        {\n" + "          \"providerVersion\": \"V1\",\n"
				+ "          \"address\": \"https://index.docker.io\",\n" + "          \"trackDigests\": false,\n"
				+ "          \"insecureRegistry\": false,\n" + "          \"cacheIntervalSeconds\": 30,\n"
				+ "          \"password\": \"Networks123!\",\n" + "          \"repositories\": [\n"
				+ "            \"opsmx11/restapp\"\n" + "          ],\n" + "          \"permissions\": {},\n"
				+ "          \"clientTimeoutMillis\": 60000,\n" + "          \"name\": \"my-docker-registry\",\n"
				+ "          \"sortTagsByDate\": false,\n" + "          \"cacheThreads\": 1,\n"
				+ "          \"paginateSize\": 100,\n" + "          \"requiredGroupMembership\": [],\n"
				+ "          \"email\": \"fake.email@spinnaker.io\",\n" + "          \"username\": \"opsmx11\"\n"
				+ "        }\n" + "      ],\n" + "      \"enabled\": true,\n"
				+ "      \"primaryAccount\": \"my-docker-registry\"\n" + "    },\n" + "    \"cloudfoundry\": {\n"
				+ "      \"accounts\": [],\n" + "      \"enabled\": false\n" + "    },\n" + "    \"google\": {\n"
				+ "      \"bakeryDefaults\": {\n" + "        \"zone\": \"us-central1-f\",\n"
				+ "        \"templateFile\": \"gce.json\",\n" + "        \"baseImages\": [],\n"
				+ "        \"network\": \"default\",\n" + "        \"useInternalIp\": false\n" + "      },\n"
				+ "      \"accounts\": [],\n" + "      \"enabled\": false\n" + "    },\n" + "    \"aws\": {\n"
				+ "      \"accessKeyId\": \"AKIAI7WBDEUL2R4EVNFA\",\n"
				+ "      \"secretAccessKey\": \"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\",\n"
				+ "      \"defaults\": {\n" + "        \"iamRole\": \"BaseIAMRole\"\n" + "      },\n"
				+ "      \"bakeryDefaults\": {\n" + "        \"baseImages\": []\n" + "      },\n"
				+ "      \"accounts\": [\n" + "        {\n" + "          \"accountId\": \"732813442182\",\n"
				+ "          \"providerVersion\": \"V1\",\n" + "          \"regions\": [],\n"
				+ "          \"permissions\": {},\n" + "          \"name\": \"my-aws-ec2-account\",\n"
				+ "          \"assumeRole\": \"role/spinnakerManaged\",\n"
				+ "          \"requiredGroupMembership\": []\n" + "        },\n" + "        {\n"
				+ "          \"accountId\": \"732813442182\",\n" + "          \"providerVersion\": \"V1\",\n"
				+ "          \"regions\": [],\n" + "          \"permissions\": {},\n"
				+ "          \"name\": \"ec2account\",\n" + "          \"assumeRole\": \"role/spinnakerManaged\",\n"
				+ "          \"requiredGroupMembership\": []\n" + "        }\n" + "      ],\n"
				+ "      \"defaultKeyPairTemplate\": \"{{name}}-keypair\",\n" + "      \"enabled\": false,\n"
				+ "      \"primaryAccount\": \"my-aws-ec2-account\",\n" + "      \"defaultRegions\": [\n"
				+ "        {\n" + "          \"name\": \"us-west-2\"\n" + "        },\n" + "        {\n"
				+ "          \"name\": \"us-east-1\"\n" + "        }\n" + "      ]\n" + "    },\n" + "    \"dcos\": {\n"
				+ "      \"accounts\": [],\n" + "      \"enabled\": false,\n" + "      \"clusters\": []\n" + "    },\n"
				+ "    \"azure\": {\n" + "      \"bakeryDefaults\": {\n"
				+ "        \"templateFile\": \"azure-linux.json\",\n" + "        \"baseImages\": []\n" + "      },\n"
				+ "      \"accounts\": [],\n" + "      \"enabled\": false\n" + "    }\n" + "  }\n" + "}";
		
		
		
		JSONParser parser = new JSONParser();
		JSONObject cloudProviders1 = null;
		try {
			cloudProviders1 = (JSONObject) parser.parse(test);

		} catch (ParseException e1) {
			System.out.println("Exception while parsing halconfig object");
			e1.printStackTrace();
		}

		System.out.println(tu.findProviderObj(cloudProviders1, "devk8s-v2local"));
		
		Map<String, JSONObject> currentCloudProviderObj = tu.findProviderObj(cloudProviders1, "devk8s-v2local");
		
		
		Iterator<Entry<String, JSONObject>> it = currentCloudProviderObj.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>)it.next(); //current entry in a loop
			String providerName = (String) entry.getKey();
			JSONObject providerObj = (JSONObject) entry.getValue();
			
			System.out.println("jjjjjjjjjj"+ providerName + "kkk\n" +  providerObj);
		}
		
		/*
		 * @SuppressWarnings("unchecked") Map.Entry<String, JSONObject> entry =
		 * (HashMap.Entry<String, JSONObject>) currentCloudProviderObj .entrySet();
		 * String providerName = entry.getKey(); JSONObject providerObj =
		 * entry.getValue();
		 */

	}
}
