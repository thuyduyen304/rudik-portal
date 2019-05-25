package com.rudik.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
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

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.predicate.analysis.KBPredicateSelector;
import asu.edu.rule_miner.rudik.predicate.analysis.SparqlKBPredicateSelector;
import asu.edu.rule_miner.rudik.rule_generator.DynamicPruningRuleDiscovery;

@RestController
@RequestMapping(value = "/api/rules")
public class RuleRestController {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final RuleDAL ruleDAL;
	
	@Autowired
  RuleRepository ruleRepository;

  private DynamicPruningRuleDiscovery naive;
  private KBPredicateSelector kbAnalysis;

	public RuleRestController(@Value("${app.rudikConfig}") String config, RuleRepository ruleRepository, RuleDAL ruleDAL) {
		this.ruleDAL = ruleDAL;
		ConfigurationFacility.setConfigurationFile(config);
		kbAnalysis = new SparqlKBPredicateSelector();
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
	
	@RequestMapping("/all-predicates")
	public Set<String> getPredicateList3() {
		return kbAnalysis.getAllPredicates();
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
			@RequestBody Rule rule) {
List<HashMap<String, Rule>> final_rule = new ArrayList<HashMap<String, Rule>>();
		
		String predicate = rule.getPredicate();  	
  	String premise = rule.getPremise();
  	// Premise
		if(premise != "") {
			List<Atom> premiseTriples = getPremiseTriples(premise);	 		
			if (premiseTriples.isEmpty()) {  		
	  		final_rule.add(new HashMap<String, Rule>()
	  		{{
	  			put("invalid", rule);
	  		}});
	  		
	  		return final_rule;
	  	}
			
			rule.setPremiseTriples(premiseTriples);
		}
		
		// Predicate
		if(predicate != "") {    	
    	Atom triple = new Atom("subject", predicate, "object");
    	rule.setConclusionTriple(triple);
    	rule.setConclusion(predicate + "(subject,object)");
		}
  	
		Integer hashcode = rule.hashCode();
  	List<Rule> check_exist = ruleRepository.findByHashcode(hashcode);
		if (check_exist.size() > 0) {
			//rule exist
			final_rule.add(new HashMap<String, Rule>()
  		{{
  			put("exist", check_exist.get(0));
  		}});
  		
  		return final_rule;
		} 
 		 		
  	// Check score.
  	Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(rule.getPredicate());
  	String typeSubject = subjectObjectType.getLeft();
    String typeObject = subjectObjectType.getRight();
  	Set<String> set_relations = Sets.newHashSet(rule.getPredicate());
    Set<RuleAtom> rule_atom = HornRule.readHornRule(rule.getPremise());
  	naive = new DynamicPruningRuleDiscovery();
    Double score = naive.getRuleConfidence(rule_atom, set_relations, typeSubject, typeObject, rule.getRuleType());
    
    // Computed Confidence
		rule.setComputedConfidence(score);
		rule.setHashcode(hashcode);
		rule.setStatus(false);
		
		final_rule.add(new HashMap<String, Rule>()
		{{
			put("new", ruleRepository.save(rule));
		}});
		
		return final_rule;
		
	}
	
	@RequestMapping("get-score")
	public List<HashMap<String, Rule>> getScore(@RequestBody Rule rule) {
		List<HashMap<String, Rule>> final_rule = new ArrayList<HashMap<String, Rule>>();
		
		String predicate = rule.getPredicate();  	
  	String premise = rule.getPremise();
  	// Premise
		if(premise != "") {
			List<Atom> premiseTriples = getPremiseTriples(premise);	 		
			if (premiseTriples.isEmpty()) {  		
	  		final_rule.add(new HashMap<String, Rule>()
	  		{{
	  			put("invalid", rule);
	  		}});
	  		
	  		return final_rule;
	  	}
			
			rule.setPremiseTriples(premiseTriples);
		}
		
		// Predicate
		if(predicate != "") {    	
    	Atom triple = new Atom("subject", predicate, "object");
    	rule.setConclusionTriple(triple);
    	rule.setConclusion(predicate + "(subject,object)");
		}
  		
  	List<Rule> check_exist = ruleRepository.findByHashcode(rule.hashCode());
  	System.out.println("test hash:" + rule.hashCode());
		if (check_exist.size() > 0) {
			//rule exist
			final_rule.add(new HashMap<String, Rule>()
  		{{
  			put("exist", check_exist.get(0));
  		}});
  		
  		return final_rule;
		} 
 		 		
  	// Check score.
  	Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(rule.getPredicate());
  	String typeSubject = subjectObjectType.getLeft();
    String typeObject = subjectObjectType.getRight();
  	Set<String> set_relations = Sets.newHashSet(rule.getPredicate());
    Set<RuleAtom> rule_atom = HornRule.readHornRule(rule.getPremise());
  	naive = new DynamicPruningRuleDiscovery();
    Double score = naive.getRuleConfidence(rule_atom, set_relations, typeSubject, typeObject, rule.getRuleType());
    
    // Computed Confidence
		rule.setComputedConfidence(score);		
  		
  	final_rule.add(new HashMap<String, Rule>()
		{{
			put(String.valueOf(score), rule);
		}});
  	
		return final_rule;
	}
	
	public List<Atom> getPremiseTriples(String premise) {
		String[] pre = premise.split("&"); 
		String subject = "";
		String object = "";
		String predicate = "";
		Atom triple = new Atom(subject, predicate, object);
		List<Atom> premiseTriples = new ArrayList<Atom>();
		for (int j = 0; j < pre.length; j++) {
			String pattern = "(^[http:\\/\\/]+[a-zA-Z:\\/.]+|^[=<>]+)([(]+)([a-z0-9]+)([,]+)([a-z0-9]+)(\\))";
			// Create a Pattern object
      Pattern triplesPattern = Pattern.compile(pattern);
      // Now create matcher object.
      Matcher triplesMatcher = triplesPattern.matcher(pre[j].trim());
	    if (triplesMatcher.find( )) {
				predicate = triplesMatcher.group(1);
				subject = triplesMatcher.group(3);
				object = triplesMatcher.group(5);
				triple = new Atom(subject, predicate, object);
				premiseTriples.add(triple);
      }
		}
		
		return premiseTriples;
	}
	
	@Secured({"ROLE_ADMIN"})
	@PostMapping("rule-approve")
	public List<Rule> getRuleApproveList(
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
    	criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "status");
			     put("op", "NOT_NULL");
			}});
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
	
	@Secured({"ROLE_ADMIN"})
	@PutMapping("/approve/{id}")
	public Rule approveRule(@RequestBody Boolean status, @PathVariable(value="id") String id) {
		Rule rule = new Rule();
		if (status) {
			rule = ruleDAL.updateStatus(id, false);
		}
		else {
			rule = ruleDAL.updateStatus(id, true);
		}

		return rule;
	}
	
	@Secured({"ROLE_ADMIN"})
	@PutMapping("/change-status/{status}")
	public Rule changeStatusRules(@RequestBody List<String> ids, @PathVariable(value="status") Boolean status) {
		Rule rule = new Rule();
		for (int i = 0; i < ids.size(); i++) {
			rule = ruleDAL.updateStatus(ids.get(i), status);
		}

		return rule;
	}
	
}
