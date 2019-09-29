package com.rudik.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rudik.dal.InstanceDAL;
import com.rudik.dal.RuleDAL;
import com.rudik.model.Instance;
import com.rudik.model.Rule;

@RestController
@RequestMapping(value = "/api/instances")
public class InstanceRestController {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final InstanceDAL instanceDAL;
	private final RuleDAL ruleDAL;

	public InstanceRestController(InstanceDAL InstanceDAL, RuleDAL RuleDAL) {
		this.instanceDAL = InstanceDAL;
		this.ruleDAL = RuleDAL;
	}
	
	@RequestMapping("/rule-samples/{id}")
	public List<Instance> sampleRule(@PathVariable(value="id") String rule_id) {
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id, "sample_instances");

		return instances;
	}
	
	@PutMapping("/{id}")
	public Instance updateLabel(@RequestBody Instance changes, @PathVariable(value="id") String id) {
		Instance instance = new Instance();
		instance = instanceDAL.updateLabel(id, changes);

		return instance;
	}
	
  @PutMapping("/compute_hc/{id}")
	public Double computeHumanConfidence(Rule changes, @PathVariable(value="id") String id) {
		Rule rule = new Rule();
		List<Instance> instances = instanceDAL.getInstanceByRuleId(id, "sample_instances");
		Double hc = 0.;
		for (Instance instance : instances)  
    { 
			hc += instance.getLabel();
    } 
		hc = hc / 20;
		changes.setHuman_confidence(hc);
		rule = ruleDAL.updateQualityEvaluation(id, changes);

		return hc;
	}
	
}
