package com.opsmx.terraApp;

import java.io.FileReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opsmx.terraApp.service.TerraformService;

@RestController
@RequestMapping("/api")
public class RequestController {

	@RequestMapping(value = "/startTerraform", method = RequestMethod.GET)
	public String startTerraformGet() throws IOException, InterruptedException {

		//TerraformService TerraformService = new TerraformService();
		//return TerraformService.runThroughCMD();
		return "{Info : " + "call post API" + "}";
	}

	@RequestMapping(value = "/startTerraform", method = RequestMethod.POST)
	public String startTerraformPost(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {

		TerraformService TerraformService = new TerraformService();
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		System.out.println("Base URL: " + baseURL);
		return TerraformService.runThroughGITSource(payload, baseURL);
	}

	@RequestMapping(value = "/statusTerraform/{statusID}", method = RequestMethod.GET)
	public String statusTerraformGet(HttpServletRequest request,@PathVariable String statusID) throws IOException, InterruptedException {

		TerraformService TerraformService = new TerraformService();
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		System.out.println("Base URL: " + baseURL);
		return TerraformService.terraformStatus(statusID, baseURL);
	}

	@RequestMapping(value = "/statusTerraformU/{statusID}", method = RequestMethod.GET)
	public String statusTerraformUGet(@PathVariable String statusID) throws IOException, InterruptedException {

		TerraformService TerraformService = new TerraformService();
		return TerraformService.terraformStatusU(statusID);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		RequestController rc = new RequestController();

		String currentSatusDir = "/home/opsmx/lalit/work/opsmx/Onboarding-Spinnaker-API/terraApp/tempCurrent/1548413082004/status";
		System.out.println("In terraform status current file path :: " + currentSatusDir);
		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;

		jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
		statusStr = (String) jsonObj.get("output");
		System.out.println(statusStr);

	}

}
