package com.rudik.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rudik.dal.RuleDAL;
import com.rudik.model.Atom;
import com.rudik.model.Rule;
import com.rudik.model.Vote;
import com.rudik.model.VotingCount;
import com.rudik.utils.Parser;
import com.rudik.utils.RuleMiningSystem;

@RestController
@RequestMapping(value = "/api")
public class RuleRestController {

	private final RuleDAL ruleDAL;
	private final RuleMiningSystem miningSystem;

	public RuleRestController(RuleDAL ruleDAL, RuleMiningSystem miningSystem) {
		this.ruleDAL = ruleDAL;
		this.miningSystem = miningSystem;
	}
	
	/**
	 * Get predicates in the DB
	 * @param knowledge_base
	 * @return list of predicates
	 */
	@RequestMapping("predicates")
	public List<String> getAllPredicate(@RequestParam(required = false, defaultValue="") String knowledge_base) {
		return ruleDAL.getAllPredicates(knowledge_base);
	}
	
	/**
	 * Get rules
	 * @param search_criteria
	 * @return rules satisfying the criteria
	 */
	@RequestMapping("rules")
	public List<Rule> getRuleList(@RequestParam Map<String,String> search_criteria) {
		List<HashMap<String, Object>> criteria = new ArrayList<HashMap<String, Object>>();
		if(search_criteria.get("knowledge_base") != null && !search_criteria.get("knowledge_base").isEmpty()) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "knowledge_base");
			     put("op", "=");
			     put("value", search_criteria.get("knowledge_base"));
			}});
    	}
    	if(search_criteria.get("predicate") != null && !search_criteria.get("predicate").isEmpty()) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "predicate");
			     put("op", "=");
			     put("value", search_criteria.get("predicate"));
			}});
    	}
    	if(search_criteria.get("type") != null && !search_criteria.get("type").isEmpty()) {
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "rule_type");
			     put("op", "=");
			     put("value", (search_criteria.get("type").equals("1")));
			}});
    	}
    	
    	if(search_criteria.get("status") != null && !search_criteria.get("status").isEmpty()) {
    		String rule_status = search_criteria.get("status").trim();
    		if (rule_status.equals("0") || rule_status.equals("1")) {
    			criteria.add(new HashMap<String, Object>()
        		{{
      	        	  put("field", "status");
      				  put("op", "=");
      				  put("value", (rule_status.equals("1")));
        		}});
    		}
    		
    	}
    	
    	Double human_confidence_from = null; 
    	Double human_confidence_to = null;
    	try {
    		human_confidence_from = Double.parseDouble(search_criteria.get("human_confidence_from"));
    	} catch(Exception e) {
//    		human_confidence_from = 0.0;
    	}
    	try {
    		human_confidence_to = Double.parseDouble(search_criteria.get("human_confidence_to"));
    	} catch(Exception e) {
//    		human_confidence_to = 1.0;
    	}
    	if(human_confidence_from != null || human_confidence_to != null) {
    		Double[] human_confidence_value = new Double[]{
    				human_confidence_from != null ? human_confidence_from : 0, 
    						human_confidence_to != null ? human_confidence_to : 1};
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "human_confidence");
			     put("op", "BETWEEN");
			     put("value", human_confidence_value);
			}});
    	}
    	
    	Date modified_from = null;
    	Date modified_to = null;
    	try {
    		modified_from = new SimpleDateFormat("yyyy-MM-dd").parse(search_criteria.get("modified_date_from"));
    	} catch(Exception e) {}
    	try {
    		modified_to = new SimpleDateFormat("yyyy-MM-dd").parse(search_criteria.get("modified_date_to"));
    	} catch(Exception e) {}
    	
    	if(modified_from != null || modified_to != null) {
    		Date[] modified_values = new Date[]{(modified_from != null ? modified_from : new Date(0L)),
		    		 (modified_to != null ? modified_to : new Date())};
    		criteria.add(new HashMap<String, Object>()
			{{
			     put("field", "last_updated");
			     put("op", "BETWEEN");
			     put("value", modified_values);
			}});
    	}
    	
		return ruleDAL.getRulesByCriteria(criteria);
	}
	
	/**
	 * Add a new rule
	 * @param rule
	 * @return the added rule
	 * @throws Exception
	 */
	@PostMapping("rules")
	public List<HashMap<String, Rule>> addRule(@RequestBody Rule rule) throws Exception {
		List<HashMap<String, Rule>> final_rule = new ArrayList<HashMap<String, Rule>>();
		String predicate = rule.getPredicate();  	
	  	String premise = rule.getPremise();
	  	// Premise
		if(premise != "") {
			Set<Atom> premiseTriples = Parser.premise_to_atom_list(premise);		
			if (premiseTriples.isEmpty()) {  		
		  		final_rule.add(new HashMap<String, Rule>()
		  		{{
		  			put("invalid", rule);
		  		}});
		  		
		  		return final_rule;
			}
			
			rule.setPremise_triples(premiseTriples);
		}
		
		// Predicate
		if(predicate != "") {    	
	    	Atom triple = new Atom("subject", predicate, "object");
	    	rule.setConclusion_triple(triple);
	    	rule.setConclusion(triple.toString());
		}
		
		if (!rule.valid()) {  		
	  		final_rule.add(new HashMap<String, Rule>()
	  		{{
	  			put("invalid", rule);
	  		}});
	  		
	  		return final_rule;
		}
  	
		Integer hashcode = rule.hashCode();
		Rule check_exist = ruleDAL.getRuleByHashcode(hashcode);
		if (check_exist != null) {
			//rule exist
			final_rule.add(new HashMap<String, Rule>()
	  		{{
	  			put("exist", check_exist);
	  		}});
	  		
	  		return final_rule;
		} 
 		 		
		// Check score.
		Double score = miningSystem.getScore(rule);
	    
	    // Computed Confidence
		rule.setComputed_confidence(score);
		rule.setHashcode(hashcode);
		rule.setStatus(false);
		
		rule.setSource(Arrays.asList(new String[]{"user"}));
		
		final_rule.add(new HashMap<String, Rule>()
		{{
			put("new", ruleDAL.saveRule(rule));
		}});
		
		return final_rule;
		
	}
	
	/**
	 * Get score of a posted rule
	 * @param rule
	 * @return score of posted rule
	 * @throws Exception
	 */
	@PostMapping("rules/computed-confidence")
	public List<HashMap<String, Rule>> getScore(@RequestBody Rule rule) throws Exception {
		List<HashMap<String, Rule>> final_rule = new ArrayList<HashMap<String, Rule>>();
		
		String predicate = rule.getPredicate();  	
		String premise = rule.getPremise();
		// Premise
		if(premise != "") {
			Set<Atom> premiseTriples = Parser.premise_to_atom_list(premise); // getPremiseTriples(premise);	 		
			if (premiseTriples.isEmpty()) {  		
	  		final_rule.add(new HashMap<String, Rule>()
	  		{{
	  			put("invalid", rule);
	  		}});
	  		
	  		return final_rule;
	  	}
			
			rule.setPremise_triples(premiseTriples);
		}
		
		// Predicate
		if(predicate != "") {    	
    	Atom triple = new Atom("subject", predicate, "object");
    	rule.setConclusion_triple(triple);
    	rule.setConclusion(predicate + "(subject,object)");
		}
  		
		Rule check_exist = ruleDAL.getRuleByHashcode(rule.hashCode());
		if (check_exist != null) {
			//rule exist
			final_rule.add(new HashMap<String, Rule>()
	  		{{
	  			put("exist", check_exist);
	  		}});
			System.out.print("debug");
	  		System.out.print(final_rule);
	  		return final_rule;
		} 
 		 		
	  	// Check score.
		Double score = miningSystem.getScore(rule);
    
	    // Computed Confidence
		rule.setComputed_confidence(score);		
  		
		final_rule.add(new HashMap<String, Rule>()
		{{
			put(String.valueOf(score), rule);
		}});
  	
		return final_rule;
	}
	
	/**
	 * Change status of a rule
	 * @param status
	 * @param id
	 * @return the updated rule
	 */
	@Secured({"ROLE_ADMIN"})
	@PutMapping("rules/{id}/status")
	public Rule approveRule(@RequestBody Boolean status, @PathVariable(value="id") String id) {
		Rule rule = new Rule();
		rule = ruleDAL.updateStatus(id, status);

		return rule;
	}
	
	/**
	 * Change status of a batch of rules
	 * @param ids
	 * @param status
	 * @return list of changed rules id
	 */
	@Secured({"ROLE_ADMIN"})
	@PutMapping("rules/batch/status")
	public List<String> changeStatusRules(@RequestBody Map<String, Object> params) {
		ArrayList<String> done = new ArrayList<String>();
		System.out.print(params);
		if (params.containsKey("ids") && params.containsKey("status")) {
			try {
				List<String> ids = (List<String>) params.get("ids");
				boolean status = (boolean) params.get("status");
				for (int i = 0; i < ids.size(); i++) {
					Rule rule = ruleDAL.updateStatus(ids.get(i), status);
					System.out.println(ids.get(i));
					done.add(ids.get(i));
				}
			} catch(Exception e) {}
		}
		return done;
	}
	
	/**
	 * Get a rule by its id
	 * @param id
	 * @return coressponding rule
	 */
	@RequestMapping("rules/{id}")
	public Rule getRule(@PathVariable(value="id") String id) {
		return ruleDAL.getRuleById(id);
	}
	
	/**
	 * update human confidence
	 * @param hc
	 * @param id
	 * @return updated rule
	 */
	@PutMapping("rules/{id}/human-confidence")
	public Rule updateHumanConfidence(@RequestBody Map<String, Double> hc, @PathVariable(value="id") String id) {
		if (hc.containsKey("human_confidence")) {
			Rule rule = ruleDAL.getRuleById(id);
			rule.setHuman_confidence(hc.get("human_confidence"));
			rule = ruleDAL.saveRule(rule);
			return rule;
		}

		return null;
	}
	
	/**
	 * update a rule
	 * @param changes
	 * @param id
	 * @return updated rule
	 */
	@PutMapping("rules/{id}/quality-evaluation")
	public Rule updateQualityEvaluation(@RequestBody Map<String, Short> qe, @PathVariable(value="id") String id) {
		if (qe.containsKey("quality_evaluation")) {
			Rule rule = ruleDAL.getRuleById(id);
			rule.setQuality_evaluation(qe.get("quality_evaluation"));
			rule = ruleDAL.saveRule(rule);
			return rule;
		}

		return null;
	}
	
	/**
	 * get the rating of a rule
	 * @param id
	 * @return rating
	 */
	@RequestMapping({"rules/{id}/rating"})
	public List<VotingCount> getVotes(@PathVariable("id") String id) { 
		return this.ruleDAL.getVotes(id); 
	}
	
	/**
	 * Change the rating of a rule
	 * @param vote
	 * @param id
	 * @param request
	 * @return the rating
	 */
	@Secured({"ROLE_ADMIN"})
	@PutMapping({"rules/{id}/rating"})
	public Vote voteRule(@RequestBody Vote vote, @PathVariable("id") String id, HttpServletRequest request) {
	    if (request.getHeader("X-FORWARDED-FOR") == null) {
	      vote.setIp(request.getRemoteHost());
	    } else {
	      String ip = request.getHeader("X-FORWARDED-FOR");
	      String[] ips = ip.split(",");
	      vote.setIp(ips[0].trim());
	    } 
	    vote.setRuleId(id);

	    return this.ruleDAL.updateVote(vote);
	}
	
	/**
	 * Get the sparql query of a rule
	 * @param id
	 * @return query
	 */
	@RequestMapping("rules/{id}/sparqlquery")
	public Map<String, String> getSparqlQuery(@PathVariable(value="id") String id) {
		Rule rule = ruleDAL.getRuleById(id);
		String query = miningSystem.getSparqlQuery(rule);
		return Collections.singletonMap("response", query);
	}
	
	/**
	 * get the computed confidence of a rule
	 * @param id
	 * @return the new score
	 */
	@GetMapping({"rules/{id}/computed-confidence"})
	public String getRuleSupport(@PathVariable(value="id") String id) {
		String result = "";
		Rule rule = ruleDAL.getRuleById(id);
	    try {
	    	Double score = miningSystem.getScore(rule);
		    result = score.toString();
	    } catch(Exception e) {
	    	result = e.getMessage();
	    }
	    return result;
	}
	
	/**
	 * update the computed confidence of a rule
	 * @param score
	 * @param id
	 * @return updated rule
	 */
	@Secured({"ROLE_ADMIN"})
	@PutMapping({"rules/{id}/computed-confidence"})
	public Rule updateRuleSupport(@RequestBody Double score, @PathVariable(value="id") String id) {
		Rule rule = ruleDAL.getRuleById(id);
		rule.setComputed_confidence(score);
	    ruleDAL.saveRule(rule);
	    return rule;
	}
	
	/**
	 * Update hashcode of all rules in the db
	 * @return
	 */
	@Secured({"ROLE_ADMIN"})
	@RequestMapping({"rules/all/update/hashcode"})
    public String updateHashcode() {
		List<Rule> rules = ruleDAL.getAllRules();
		int i = 0;
		for(Rule r: rules) {
			Integer new_hashcode = r.hashCode();
			r.setHashcode(new_hashcode);
			ruleDAL.saveRule(r);
			i++;
		}
		
		return "done for " + i + " rules";
    }
	
	/**
	 * Trim rules' premise
	 * @return
	 */
	@Secured({"ROLE_ADMIN"})
	@RequestMapping({"rules/all/update/premise"})
    public String updatePremise() {
		List<Rule> rules = ruleDAL.getAllRules();
		int i = 0;
		for(Rule r: rules) {
			String premise = r.getPremise().trim();
			premise = premise.replace("  ", " ");
			r.setPremise(premise);
			ruleDAL.saveRule(r);
			i++;
		}
		
		return "done for " + i + " rules";
    }
	
	/**
	 * update computed_confidence of rules of a predicate
	 * @param predicate
	 * @return
	 */
	@Secured({"ROLE_ADMIN"})
	@PutMapping({"rules/batch/computed-confidence"})
    public String updateSupport(
			@RequestBody String predicate) {
		String result = "";
		List<HashMap<String, Object>> criteria = new ArrayList<HashMap<String, Object>>();
		criteria.add(new HashMap<String, Object>()
		{{
		     put("field", "predicate");
		     put("op", "=");
		     put("value", predicate);
		}});
		List<Rule> rules = ruleDAL.getRulesByCriteria(criteria);

	  	int i = 0;
	  	for(Rule rule : rules) {
		  	try {
		  		Double score = miningSystem.getScore(rule);
		  		rule.setComputed_confidence(score);
		  		ruleDAL.saveRule(rule);
		  		i++;
		  	} catch(Exception e) {
		  		result += e.getMessage();
		  	}
	  	}
		
		return "done for " + i + " rules\n" + result;
    }
	
}
