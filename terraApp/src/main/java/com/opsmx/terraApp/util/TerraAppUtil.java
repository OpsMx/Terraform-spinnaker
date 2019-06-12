package com.opsmx.terraApp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class TerraAppUtil {

	@SuppressWarnings("unchecked")
	public File createDirForPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = applicaton;
		String PipelineName = pipeline;
		String PipelineId = pipelineId;
		
		String currentUserDir = System.getProperty("user.home");
		System.out.println("Present Dir " + currentUserDir);
		String opsmxdir = currentUserDir + "/.opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File spinnakerDirFile = new File(opsmxDirFile.getPath() + "/" + "spinnaker");
		if (!spinnakerDirFile.exists())
			spinnakerDirFile.mkdir();

		File applicationDirFile = new File(spinnakerDirFile.getPath() + "/" + ApplicationName);
		if (!applicationDirFile.exists())
			applicationDirFile.mkdir();

		File pipelineNameDirFile = new File(applicationDirFile.getPath() + "/" + PipelineName);
		if (!pipelineNameDirFile.exists())
			pipelineNameDirFile.mkdir();

		File pipelineIdDirFile = new File(pipelineNameDirFile.getPath() + "/" + PipelineId);
		if (!pipelineIdDirFile.exists())
			pipelineIdDirFile.mkdir();

		String statusFilePath = pipelineIdDirFile.getPath() + "/status";
		File statusFile = new File(statusFilePath);
		if (!statusFile.exists()) {
			try {
				statusFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JSONObject statusRootObj = new JSONObject();
		statusRootObj.put("status", "running");

		InputStream statusInputStream = new ByteArrayInputStream(
				statusRootObj.toString().getBytes(StandardCharsets.UTF_8));
		writeStreamOnFile(statusFile, statusInputStream);
		
		return pipelineIdDirFile;
	}

	public String getDirPathOfPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = "applicationName-" + applicaton;
		String PipelineName = "pipelineName-" + pipeline;
		String PipelineIdName = "pipelineId-" + pipelineId;
		String currentUserDir = System.getProperty("user.home");
		String pipelineIdDir = currentUserDir + "/.opsmx/spinnaker/" + ApplicationName + "/" + PipelineName + "/"
				+ PipelineIdName;

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

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		TerraAppUtil tu = new TerraAppUtil();
		tu.createDirForPipelineId("test", "depl2oy", "excg4587ut");
		System.out.println("Properties " + System.getProperties().toString());
		System.out.println(tu.createDirForPipelineId("test", "deploy", "excg4587ut"));
	}
}
