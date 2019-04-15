package com.rudik.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rudik.dal.InstanceDAL;
import com.rudik.dal.InstanceRepository;
import com.rudik.model.Instance;

@Controller
@RequestMapping(value = "/instance")
public class InstanceController {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final InstanceRepository instanceRepository;

	private final InstanceDAL instanceDAL;

	public InstanceController(InstanceRepository instanceRepository, InstanceDAL instanceDAL) {
		this.instanceRepository = instanceRepository;
		this.instanceDAL = instanceDAL;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/sample")
	public void export(@RequestParam(name = "rule_id", required = false, defaultValue = "a") String rule_id,
			HttpServletResponse response) throws IOException {
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id, "sample_instances");
		ObjectWriter mapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String myString = mapper.writeValueAsString(instances);

		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment;filename=instances_" + rule_id + ".json");

		PrintWriter out = response.getWriter(); 
		out.println(myString);
		out.flush();
		out.close();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/all")
	public void export_all(@RequestParam(name = "rule_id", required = false, defaultValue = "a") String rule_id,
			HttpServletResponse response) throws IOException {
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id, "instances");
		ObjectWriter mapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String myString = mapper.writeValueAsString(instances);

		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment;filename=instances_" + rule_id + ".json");
		PrintWriter out = response.getWriter(); 
		out.println(myString);
		out.flush();
		out.close();
	}

}
