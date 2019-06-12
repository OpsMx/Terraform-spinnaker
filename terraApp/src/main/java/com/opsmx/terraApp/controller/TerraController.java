/**
 * 
 */
package com.opsmx.terraApp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opsmx.terraApp.service.TerraService;

/**
 * @author Lalit
 *
 */
@RestController
@RequestMapping("/api/v1")
public class TerraController {

	@Autowired
	TerraService ts;

	@RequestMapping(value = "/startTerraform", method = RequestMethod.POST)
	public String startTerraform(HttpServletRequest request, @RequestBody String payload) throws IOException, InterruptedException {
		System.out.println("{Info : " + "In startTerraform! call post API" + "} :: " + payload);
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		System.out.println("Base URL: " + baseURL);
		return ts.planStart(payload, baseURL);
	}
	
	@RequestMapping(value = "/terraform/planStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformPlanStatus(HttpServletRequest request,@PathVariable String applicationName,@PathVariable String pipelineName,@PathVariable String pipelineId) throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"	+ request.getServerPort();
		System.out.println("Base URL: " + baseURL);
		return ts.planStatus(applicationName,pipelineName,pipelineId, baseURL);
				
	}

	@RequestMapping(value = "/terraform/planOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformPlanOutput(HttpServletRequest request,@PathVariable String applicationName,@PathVariable String pipelineName,@PathVariable String pipelineId) throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"	+ request.getServerPort();
		System.out.println("Base URL: " + baseURL);
		return ts.planOutput(applicationName,pipelineName,pipelineId, baseURL);
				
	}
	
	
	@RequestMapping(value = "/deleteTerraform", method = RequestMethod.POST)
	public String deleteTerraform(HttpServletRequest request, @RequestBody String payload) throws IOException, InterruptedException {
		System.out.println("{Info : " + "In deleteTerraform! call post API" + "} :: " + payload);
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		System.out.println("Base URL: " + baseURL);
		return ts.planDelete(payload, baseURL);
	}
}
