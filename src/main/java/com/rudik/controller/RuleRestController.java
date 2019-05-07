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
import com.rudik.form.AddForm;
import com.rudik.form.SearchForm;
import com.rudik.model.Atom;
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
	
	@RequestMapping("add-rule")
	public List<HashMap<String, Rule>> addRule(
			@RequestBody AddForm addForm) {

		Rule rule = new Rule();
		
		List<HashMap<String, Object>> criteria = new ArrayList<HashMap<String, Object>>();
		if(!addForm.getKnowledgeBase().equals("none")) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "knowledgeBase");
			     put("op", "=");
			     put("value", addForm.getKnowledgeBase());
			}});
  	}
  	if(!addForm.getPredicate().equals("none")) {
  		criteria.add(new HashMap<String, Object>()
		{{
		     put("field", "predicate");
		     put("op", "=");
		     put("value", addForm.getPredicate());
		}});
  	}
  	if(addForm.getRuleType() != -1) {
  		criteria.add(new HashMap<String, Object>()
		{{
		     put("field", "ruleType");
		     put("op", "=");
		     put("value", (addForm.getRuleType() == 1));
		}});
  	}
  	if(addForm.getPremise() != "") {
  		criteria.add(new HashMap<String, Object>()
		{{
		     put("field", "premise");
		     put("op", "=");
		     put("value", addForm.getPremise());
		}});
  	}
  	
  	List<Rule> rules = ruleDAL.getRulesByCriteria(criteria);
		
  	if (!rules.isEmpty()) {
  		List<HashMap<String, Rule>> exist_rule = new ArrayList<HashMap<String, Rule>>();
  		exist_rule.add(new HashMap<String, Rule>()
  		{{
  			put("exist", rules.get(0));
  		}});
  		
  		return exist_rule;
  	}
		
		// rule type
		if (addForm.getRuleType() == 1) {
			rule.setRuleType(true);
		}
		else if (addForm.getRuleType() == 0) {
			rule.setRuleType(false);
		}
		
		// Knowledge Base
		if(!addForm.getKnowledgeBase().equals("none")) {
			rule.setKnowledgeBase(addForm.getKnowledgeBase());
		}
		
		// Premise
		if(addForm.getPremise() != "") {
			String premise = addForm.getPremise();
			rule.setPremise(premise);
			String[] pre = premise.split("&"); 
			String subject = "";
			String object = "";
			String predicate = "";
			Atom triple = new Atom(subject, predicate, object);
			List<Atom> premiseTriples = new ArrayList<Atom>();
			for (int j = 0; j < pre.length; j++) {
				subject = pre[j].trim().replaceAll(".*\\(|\\).*", "").split(",")[0];
				object = pre[j].trim().replaceAll(".*\\(|\\).*", "").split(",")[1];
				predicate = pre[j].trim().split("\\(")[0];
				triple = new Atom(subject, predicate, object);
				premiseTriples.add(triple);
			}
			
			rule.setPremiseTriples(premiseTriples);
		}
		
		// Predicate
		if(addForm.getPredicate() != "") {
			String predicate = addForm.getPredicate();
			rule.setPredicate(predicate);
    	
    	Atom triple = new Atom("subject", predicate, "object");
    	rule.setConclusionTriple(triple);
    	rule.setConclusion(predicate + "(subject,object)");
		}
		
		// Human Confidence
		if(addForm.getHumanConfidence() != null) {
			rule.setHumanConfidence(addForm.getHumanConfidence());
		}
		
		// Quality Evaluation
		if(addForm.getQualityEvaluation() != null) {
			rule.setQualityEvaluation(addForm.getQualityEvaluation());
		}
		
		// Computed Confidence
		if(addForm.getComputedConfidence() != null) {
			rule.setComputedConfidence(addForm.getComputedConfidence());
		}
		
		List<HashMap<String, Rule>> new_rule = new ArrayList<HashMap<String, Rule>>();
		new_rule.add(new HashMap<String, Rule>()
		{{
			put("new", ruleDAL.addNewRule(rule));
		}});
		
		return new_rule;
		
	}
}
