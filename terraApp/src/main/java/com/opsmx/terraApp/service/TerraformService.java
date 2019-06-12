package com.opsmx.terraApp.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.opsmx.terraApp.util.PropertiesUtil;


public class TerraformService {

	
	static String DEMO_HTML = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title>Opsmx TerraApp</title> </head> <body bgcolor='#000000'> <pre style=\"color:white;\"> \"OPTION_SCPACE\" </pre> </body> </html>";
	static Properties properties = PropertiesUtil.getInstance();
	static String SCRIPT_PATH = (properties.getProperty("app.script.location.file")!=null)? properties.getProperty("app.script.location.file"):"/tmp/exeTerraformCmd.sh";
	static String PROVIDER_DIR = (properties.getProperty("app.provider.location")!=null)? properties.getProperty("app.provider.location"):"/tmp";
	
	public String runThroughCMD() throws IOException, InterruptedException {

		Process exec = Runtime.getRuntime()
				.exec(new String[] { "/bin/sh", "-c", "printf 'yes' | sh /tmp/exeTerraformCmd.sh" });
		exec.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		System.out.println("First :: " + exec.exitValue());
		String outPut = (exec.exitValue() == 0) ? "true" : "false";

		/*
		 * Runtime r=Runtime.getRuntime(); System.out.println("No of Processor: "+
		 * r.availableProcessors());
		 * System.out.println("Total memory: "+r.totalMemory());
		 * System.out.println("Free memory: "+r.freeMemory());
		 * System.out.println("Memory occupied: "+ (r.totalMemory()-r.freeMemory()));
		 */

		return outPut;
	}

	@SuppressWarnings("unchecked")
	public String runThroughFile(String payload) throws IOException, InterruptedException {

		JSONObject jsonObj = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(payload);

		} catch (Exception e) {
			e.printStackTrace();
		}
		String terraformRemoteLocation = (String) jsonObj.get("value");
		String terraformProvider = (String) jsonObj.get("provider");
		System.out.println("Plan file URL :: " + terraformRemoteLocation);

		URL terraformURL = new URL(terraformRemoteLocation);
		InputStream inputStream = terraformURL.openStream();
		File file = createPlanFile(inputStream, terraformProvider);

		// String scriptPathInStr =
		// classLoader.getResource("/tmp/exeTerraformCmd.sh").getPath();
		String scriptPathInStr = "/tmp/exeTerraformCmd.sh";
		System.out.println("Script file path: " + scriptPathInStr);
		String exactScriptPath = scriptPathInStr;

		System.out.println("Script file path" + exactScriptPath);
		System.out.println("Terraform plan file path" + file.getPath());

		Process exec = Runtime.getRuntime()
				.exec(new String[] { "/bin/sh", "-c", "printf 'yes' | sh " + exactScriptPath + " " + file.getPath() });

		exec.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line + "\n" + line.trim();
			System.out.println(line);
		}

		BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
		String line2;
		while ((line2 = reader2.readLine()) != null) {
			System.out.println("error");
			line2 = line2 + "\n" + line2.trim();
			System.out.println(line2);
		}
		System.out.println("First :: " + exec.exitValue());

		JSONObject statusRootObj = new JSONObject();
		if (exec.exitValue() == 0) {
			statusRootObj.put("status", "true");
			statusRootObj.put("output", line);
		} else {
			statusRootObj.put("status", "false");
			statusRootObj.put("output", line2);
		}

		String outPut = (exec.exitValue() == 0) ? "true" : "false";

		reader.close();
		reader2.close();

		return outPut;
	}

	@SuppressWarnings({ "unchecked" })
	public String runThroughGITSource(String payload, String baseURL) throws IOException, InterruptedException {

		JSONObject jsonObj = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(payload);

		} catch (Exception e) {
			e.printStackTrace();
		}
		String terraformSource = (String) jsonObj.get("source");
		String terraformModule = (String) jsonObj.get("module");
		String terraformProvider = (String) jsonObj.get("provider");

		String temp1 = new String("module \"SOURCE_MODULE\"{source = \"git::SOURCE_RESOURCE_PATH\"}");
		System.out.println("*****************");
		System.out.println("Terraform module string before change :: " + temp1);

		String temp2 = temp1.replaceAll("SOURCE_MODULE", terraformModule);
		String terraformModuleTemplate = temp2.replaceAll("SOURCE_RESOURCE_PATH", terraformSource);

		System.out.println("Terraform module string after change :: " + terraformModuleTemplate);
		System.out.println("*****************");
		InputStream inputStream = new ByteArrayInputStream(terraformModuleTemplate.getBytes(StandardCharsets.UTF_8));

		File file = createPlanFile(inputStream, terraformProvider);

		TerraformServiceThread terraOperationCall = new TerraformServiceThread(file);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		String[] bits = file.getPath().split("/");
		String lastOne = bits[bits.length - 1];

		String pollURL = baseURL + "/api/statusTerraform/" + lastOne;

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "true");
		outRootObj.put("statusurl", pollURL);
		
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String terraformStatus(String payload, String baseURL) {
		String currentSatusDir = System.getProperty("user.dir") + "/tempCurrent/" + payload + "/status";
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
		
		String pollURL = baseURL + "/api/statusTerraformU/" + payload;
		outputJsonObj.put("unifiedLogURL", pollURL);
		outputJsonObj.put("progress", pollURL);
		outputJsonObj.put("Code", pollURL);
		outputJsonObj.put("Response", pollURL);
		
		System.out.println("In terraform status API block current status :: " + outputJsonObj.toJSONString());
		return outputJsonObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String terraformStatusU(String payload) {
		String currentSatusDir = System.getProperty("user.dir") + "/tempCurrent/" + payload + "/status";
		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
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
	public File createPlanFile(InputStream stream, String provider) throws IOException {

		String currentUserDir = System.getProperty("user.dir");
		
		String currentTempDir = currentUserDir + "/tempCurrent";
		File dirFile = new File(currentTempDir);
		if (!dirFile.exists())
			dirFile.mkdir();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Long currentTimestamp = timestamp.getTime();
		File dirFileWithTimestamp = new File(dirFile.getPath() + "/" + currentTimestamp);
		if (!dirFileWithTimestamp.exists())
			dirFileWithTimestamp.mkdir();

		String statusFilePath = dirFileWithTimestamp.getPath() + "/status";
		File statusFile = new File(statusFilePath);
		if (!statusFile.exists())
			statusFile.createNewFile();

		JSONObject statusRootObj = new JSONObject();
		statusRootObj.put("status", "running");

		InputStream statusInputStream = new ByteArrayInputStream(
				statusRootObj.toString().getBytes(StandardCharsets.UTF_8));
		writeStreamOnFile(statusFile, statusInputStream);

		String filePath = dirFileWithTimestamp.getPath() + "/plan.tf";
		File file = new File(filePath);
		if (!file.exists())
			file.createNewFile();
		
		writeStreamOnFile(file, stream);
		
		File tempProviderFile = null;
		File orignalProvidefile = null;
		
		if(provider.equalsIgnoreCase("k8") || provider.equalsIgnoreCase("kubernetes") || provider.equalsIgnoreCase("oc") || provider.equalsIgnoreCase("openshift") ) {
			String providerFilePath = dirFileWithTimestamp.getPath() + "/k8provider.tf";
			tempProviderFile = new File(providerFilePath);
			if (!tempProviderFile.exists())
				tempProviderFile.createNewFile();
			String PROVIDER_DIR_WITH_FILE = PROVIDER_DIR.replaceAll("\"", "").trim() + "/k8provider.tf";
			orignalProvidefile = new File(PROVIDER_DIR_WITH_FILE);
		}
		
		if(provider.equalsIgnoreCase("gcp") || provider.equalsIgnoreCase("google")) {
			String providerFilePath = dirFileWithTimestamp.getPath() + "/gcpprovider.tf";
			tempProviderFile = new File(providerFilePath);
			if (!tempProviderFile.exists())
				tempProviderFile.createNewFile();
			String PROVIDER_DIR_WITH_FILE = PROVIDER_DIR.replaceAll("\"", "").trim() + "/gcpprovider.tf";
			orignalProvidefile = new File(PROVIDER_DIR_WITH_FILE);
		}
		
		//Files.copy(Providefileobj.toPath(), dirFileWithTimestamp.toPath()+"/provider.tf", StandardCopyOption.REPLACE_EXISTING);
		FileUtils.copyFile(orignalProvidefile,tempProviderFile);
		
		return dirFileWithTimestamp;
	}

	public void writeStreamOnFile(File file, InputStream stream) throws IOException {

		boolean append = true;
		boolean autoFlush = true;
		String charset = "UTF-8";

		FileOutputStream fos = new FileOutputStream(file, append);
		OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
		BufferedWriter bw = new BufferedWriter(osw);
		PrintWriter pw = new PrintWriter(bw, autoFlush);

		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			pw.write(inputLine);
		}

		in.close();
		pw.close();

	}

	class TerraformServiceThread implements Runnable {

		private File file;

		public TerraformServiceThread(File file) {

			this.file = file;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			String exactScriptPath = SCRIPT_PATH;
			
			System.out.println("Script file path : " + exactScriptPath);
			System.out.println("Terraform plan file path : " + file.getPath());

			Process exec;
			try {
				exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c",
						"printf 'yes' | sh " + exactScriptPath + " " + file.getPath() });
				exec.waitFor();

				BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
				String line = "";
				String tempLine = "";
				while ((tempLine = reader.readLine()) != null) {
					// line = line.concat("/\n" + " ***** :: " + tempLine.trim());
					line = line + tempLine.trim() + System.lineSeparator();
				}

				BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
				String line2 = "";
				String tempLine2 = "";
				while ((tempLine2 = reader2.readLine()) != null) {
					// line2 = line2.concat("/\n" + " ***** :: " +tempLine2.trim());
					line2 = line2 + tempLine2.trim() + System.lineSeparator();
				}

				reader.close();
				reader2.close();

				JSONObject statusRootObj = new JSONObject();
				if (exec.exitValue() == 0) {
					statusRootObj.put("status", "true");
					statusRootObj.put("output", line);
				} else {
					statusRootObj.put("status", "false");
					statusRootObj.put("output", line2);
				}

				String filePath = file.getPath() + "/status";
				File statusFile = new File(filePath);
				statusFile.delete();
				InputStream statusInputStream = new ByteArrayInputStream(
						statusRootObj.toString().getBytes(StandardCharsets.UTF_8));
				writeStreamOnFile(statusFile, statusInputStream);

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
