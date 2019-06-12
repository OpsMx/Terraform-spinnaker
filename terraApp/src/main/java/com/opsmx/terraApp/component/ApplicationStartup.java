package com.opsmx.terraApp.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.opsmx.terraApp.util.TerraAppUtil;
import com.opsmx.terraApp.util.HalConfigUtil;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	/**
	 * This method is called during Spring's startup.
	 * 
	 * @param event Event raised when an ApplicationContext gets initialized or
	 *              refreshed.
	 */
	

	public String halConfigObject;
	JSONParser parser = new JSONParser();
	
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {

		System.out.println("Application Stated");
		TerraAppUtil tu = new TerraAppUtil();
		
		
		// logic to copy script in-place
		String currentUserDir = System.getProperty("user.home");
		System.out.println("Home dir of user :: " + currentUserDir);
		String opsmxdir = currentUserDir + "/.opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File scriptDirFile = new File(opsmxDirFile.getPath() + "/" + "script");
		if (!scriptDirFile.exists())
			scriptDirFile.mkdir();

		File source = new File(scriptDirFile.getPath() + "/exeTerraformCmd.sh");
		tu.overWriteStreamOnFile(source, getClass().getClassLoader().getResourceAsStream("/script/exeTerraformCmd.sh"));

		File terraformOutputSource = new File(scriptDirFile.getPath() + "/exeTerraformOutput.sh");
		tu.overWriteStreamOnFile(terraformOutputSource,
				getClass().getClassLoader().getResourceAsStream("/script/exeTerraformOutput.sh"));

		File terraformGitOutputSource = new File(scriptDirFile.getPath() + "/exeTerraformGitOutput.sh");
		tu.overWriteStreamOnFile(terraformGitOutputSource,
				getClass().getClassLoader().getResourceAsStream("/script/exeTerraformGitOutput.sh"));

		File halConfigSource = new File(scriptDirFile.getPath() + "/exeHalConfig.sh");
		tu.overWriteStreamOnFile(halConfigSource,
				getClass().getClassLoader().getResourceAsStream("/script/exeHalConfig.sh"));

		System.out.println("Hal config ::" + halConfig(halConfigSource));
		HalConfigUtil.setHalConfig(halConfig(halConfigSource));
		// logic to configure hal config

	}

	@SuppressWarnings("unchecked")
	public String halConfig(File file) {

		System.out.println("Hal config script path : " + file.getPath());

		JSONObject halConfigRootObj = new JSONObject();


		Process exec;
		try {
			exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "sh " + file.getPath() });
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

			if (exec.exitValue() == 0) {
				int startIndex = line.indexOf('{');
				String halConfigString = line.substring(startIndex);
				halConfigRootObj = (JSONObject) parser.parse(halConfigString);
				System.out.println("Successfully parse hal config");

			} else {
				halConfigRootObj.put("error", line2);
				System.out.println("Error while fetching hal config please make sure you hal daemaon is running");
			}

		} catch (IOException | InterruptedException | ParseException e) {
			e.printStackTrace();
		}

		return halConfigRootObj.toJSONString();
	}

}