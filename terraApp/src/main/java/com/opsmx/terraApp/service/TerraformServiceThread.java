package com.opsmx.terraApp.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.json.simple.JSONObject;

import com.opsmx.terraApp.util.PropertiesUtil;
import com.opsmx.terraApp.util.TerraAppUtil;

class TerraformServiceThread implements Runnable {

	private File file;
	TerraAppUtil terraAppUtil = new TerraAppUtil();
	//static Properties properties = PropertiesUtil.getInstance();
	
	public TerraformServiceThread(File file) {

		this.file = file;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		String exactScriptPath = System.getProperty("user.home") + "/.opsmx/script/exeTerraformCmd.sh";
		
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
			terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

}