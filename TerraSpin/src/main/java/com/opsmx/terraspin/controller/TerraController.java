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

package com.opsmx.terraspin.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opsmx.terraspin.service.TerraService;

@RestController
@RequestMapping("/api/v1")
public class TerraController {
	
	private static final Logger log = LoggerFactory.getLogger(TerraController.class);
	@Autowired
	TerraService ts;
    
	@RequestMapping(value = "/terraformPlan", method = RequestMethod.POST)
	public String startTerraform(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform plan payload :"+payload);
		return ts.planStart(payload, baseURL);
	}

	@RequestMapping(value = "/terraform/planStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformPlanStatus(HttpServletRequest request, @PathVariable String applicationName,
			@PathVariable String pipelineName, @PathVariable String pipelineId)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform plan status :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
		return ts.planStatus(applicationName, pipelineName, pipelineId, baseURL);

	}

	@RequestMapping(value = "/terraform/planOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformPlanOutput(HttpServletRequest request, @PathVariable String applicationName,
			@PathVariable String pipelineName, @PathVariable String pipelineId)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform plan output :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
		return ts.planOutput(applicationName, pipelineName, pipelineId, baseURL);

	}

	@RequestMapping(value = "/terraformApply", method = RequestMethod.POST)
	public String applyTerraform(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform Apply payload :"+payload);
		return ts.applyStart(payload, baseURL);
	}

	@RequestMapping(value = "/terraform/applyStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformApplyStatus(HttpServletRequest request, @PathVariable String applicationName,
			@PathVariable String pipelineName, @PathVariable String pipelineId)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform apply status applicationName:"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
		
		return ts.applyStatus(applicationName, pipelineName, pipelineId, baseURL);

	}

	@RequestMapping(value = "/terraform/applyOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformApplyOutput(HttpServletRequest request, @PathVariable String applicationName,
			@PathVariable String pipelineName, @PathVariable String pipelineId)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform apply output applicationName :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
		
		return ts.applyOutput(applicationName, pipelineName, pipelineId, baseURL);

	}

	@RequestMapping(value = "/terraformDestroy", method = RequestMethod.POST)
	public String deleteTerraform(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform destroy payload :"+payload);
		return ts.destroyStart(payload, baseURL);
	}

	@RequestMapping(value = "/terraform/destroyStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformDeleteStatus(HttpServletRequest request, @PathVariable String applicationName,
			@PathVariable String pipelineName, @PathVariable String pipelineId)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform destroy status applicationName :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
		
		return ts.destroyStatus(applicationName, pipelineName, pipelineId, baseURL);

	}

	@RequestMapping(value = "/terraform/destroyOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	public String terraformDeleteOutput(HttpServletRequest request, @PathVariable String applicationName,
			@PathVariable String pipelineName, @PathVariable String pipelineId)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform destroy output applicationName :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
		
		return ts.destroyOutput(applicationName, pipelineName, pipelineId, baseURL);

	}

}
