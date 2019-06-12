package com.opsmx.terraApp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Test {

	public void abc() throws IOException {

		//////////////

		InputStream inputStream = null;

		try {
			Properties prop = new Properties();
			String propFileName = "application.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

			// get the property value and print it out
			String user = prop.getProperty("spring.banner.location");
			System.out.println("Hi + " + user);

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}

		//////////////

	}

	public static void main(String[] args) throws IOException {

		/*
		 * String repoUrl = new
		 * String("https://github.com/OpsMx/Onboarding-Spinnaker-API.git"); String
		 * cloneDirectoryPath = new String("/home/opsmx/lalit/test/");
		 * 
		 * try{ System.out.println("Cloning " + repoUrl + " into " + repoUrl);
		 * Git.cloneRepository().setURI(repoUrl).setDirectory(Paths.get(
		 * cloneDirectoryPath).toFile()).call();
		 * System.out.println("Completed Cloning"); }catch( GitAPIException e) {
		 * System.out.println("Exception occurred while cloning repo");
		 * e.printStackTrace(); }
		 */

		Test t1 = new Test();
		t1.abc();
	}
}
