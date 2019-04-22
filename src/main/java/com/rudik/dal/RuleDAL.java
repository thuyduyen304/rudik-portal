package com.rudik.dal;

import java.util.HashMap;
import java.util.List;

import com.rudik.model.Rule;

public interface RuleDAL {
	List<Rule> getAllRules();

	Rule getRuleById(String ruleId);

	Rule addNewRule(Rule rule);
	
	List<Rule> getRuleByKnowledgeBase(String knowledge_base);
	
	List<String> getAllKnowledgeBase();
	
	List<String> getAllPredicates(String knowledge_base);
	
	List<Rule> getRulesByCriteria(List<HashMap<String, Object>> criteria);
	
	Rule updateQualityEvaluation(String rule_id, Rule changes);

}
