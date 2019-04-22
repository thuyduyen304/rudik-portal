package com.rudik.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rudik.dal.RuleDAL;
import com.rudik.dal.RuleRepository;
import com.rudik.form.SearchForm;
import com.rudik.model.Rule;

@RestController
@RequestMapping(value = "/api/rules")
public class RuleRestController {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final RuleDAL ruleDAL;

	public RuleRestController(RuleRepository ruleRepository, RuleDAL ruleDAL) {
		this.ruleDAL = ruleDAL;
	}
	
	@RequestMapping("/{knowledge_base}/predicates")
	public List<String> getPredicateList(
			@PathVariable String knowledge_base) {
		return ruleDAL.getAllPredicates(knowledge_base);
	}
	
	@RequestMapping("/predicates/{knowledgeBase}")
	public List<String> getPredicateList2(
			@PathVariable String knowledgeBase) {
		return ruleDAL.getAllPredicates(knowledgeBase);
	}
	
	@PostMapping
	public List<Rule> getRuleList(
            @RequestBody SearchForm searchForm) {
		List<HashMap<String, Object>> criteria = new ArrayList<HashMap<String, Object>>();
		if(!searchForm.getKnowledgeBase().equals("none")) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "knowledgeBase");
			     put("op", "=");
			     put("value", searchForm.getKnowledgeBase());
			}});
    	}
    	if(!searchForm.getPredicate().equals("none")) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "predicate");
			     put("op", "=");
			     put("value", searchForm.getPredicate());
			}});
    	}
    	if(searchForm.getRuleType() != -1) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "ruleType");
			     put("op", "=");
			     put("value", (searchForm.getRuleType() == 1));
			}});
    	}
    	Double human_confidence_from = searchForm.getHumanConfidenceFrom();
    	Double human_confidence_to = searchForm.getHumanConfidenceTo();
    	if(human_confidence_from != null || human_confidence_to != null) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "humanConfidence");
			     put("op", "BETWEEN");
			     put("value", new Double[]{(human_confidence_from != null ? human_confidence_from : 0.0),
			    		 (human_confidence_to != null ? human_confidence_to : 1.0)});
			}});
    	}
    	
		return ruleDAL.getRulesByCriteria(criteria);
	}
	
	@PutMapping("/{id}")
	public Rule updateRule(@RequestBody Rule changes, @PathVariable(value="id") String id) {
		Rule rule = new Rule();
		rule = ruleDAL.updateQualityEvaluation(id, changes);

		return rule;
	}
}
