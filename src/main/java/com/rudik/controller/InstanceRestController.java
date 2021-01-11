package com.rudik.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rudik.dal.InstanceDAL;
import com.rudik.dal.RuleDAL;
import com.rudik.model.Instance;
import com.rudik.model.Rule;
import com.rudik.utils.RuleMiningSystem;

@RestController
@RequestMapping(value = "/api")
public class InstanceRestController {

	private final InstanceDAL instanceDAL;
	private final RuleDAL ruleDAL;
	private RuleMiningSystem miningSystem;
	

	public InstanceRestController(
			InstanceDAL InstanceDAL, RuleDAL RuleDAL, RuleMiningSystem miningSystem) {
		this.instanceDAL = InstanceDAL;
		this.ruleDAL = RuleDAL;
		this.miningSystem = miningSystem;
	}
	
	@RequestMapping("/rules/{id}/sample-instances")
	public List<Instance> sampleRule(@PathVariable(value="id") String rule_id) {
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id);
		if(instances.size() == 0) {
			Rule rule = ruleDAL.getRuleById(rule_id);
			instances = miningSystem.getInstances(rule);
			for (Instance inst: instances) {
				instanceDAL.addNewInstance(inst);
			}
		}
		return instances;
	}
	
	@Secured({"ROLE_ADMIN"})
	@PutMapping("/instances/{id}")
	public Instance updateLabel(@RequestBody Instance changes, @PathVariable(value="id") String id) {
		Instance instance = new Instance();
		instance = instanceDAL.updateLabel(id, changes);

		return instance;
	}
	
	@GetMapping("/instances/{id}")
	public Instance getInstance(@PathVariable(value="id") String id) {
		Instance instance = instanceDAL.getInstanceById(id);

		return instance;
	}
	
	@RequestMapping("/rules/{id}/sample-instances/human-confidence")
	public Double computeHumanConfidence( @PathVariable(value="id") String id) {
		Rule rule = ruleDAL.getRuleById(id);
		if (rule != null) {
			List<Instance> instances = instanceDAL.getInstanceByRuleId(id);
			Double hc = 0.;
			for (Instance instance : instances)  
		    { 
					hc += instance.getLabel();
		    } 
			hc = hc / 20;
			rule.setHuman_confidence(hc);
			rule = ruleDAL.saveRule(rule);
			return hc;
		}
		return null;
	}
  
  	@Secured({"ROLE_ADMIN"})
	@RequestMapping({"instances/all/hashcode"})
  	public String updateHashcode() {
		List<Instance> insts = instanceDAL.getAllInstances();
		int j = 0;
		for(Instance i: insts) {
			Integer new_hashcode = i.hashCode();
			i.setHashcode(new_hashcode);
			instanceDAL.updateInstance(i);
			j++;
		}
		
		return "done for " + j + " rules";
  	}
	
}
