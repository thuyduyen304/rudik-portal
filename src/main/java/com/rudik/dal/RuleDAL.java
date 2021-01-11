package com.rudik.dal;

import java.util.HashMap;
import java.util.List;

import com.rudik.model.Rule;
import com.rudik.model.Vote;
import com.rudik.model.VotingCount;

public interface RuleDAL {
	List<Rule> getAllRules();

	Rule getRuleById(String ruleId);
	
	Rule getRuleByHashcode(Integer ruleId);

//	Rule addNewRule(Rule rule);
	
	List<Rule> getRuleByKnowledgeBase(String knowledge_base);
	
	List<String> getAllKnowledgeBase();
	
	List<String> getAllPredicates(String knowledge_base);
	
	List<Rule> getRulesByCriteria(List<HashMap<String, Object>> criteria);
	
	List<Rule> getAllValidRules();
	
//	Rule updateQualityEvaluation(String rule_id, Rule changes);
	
	Rule updateStatus(String rule_id, Boolean changes);
	
	Rule saveRule(Rule rule);
	
	List<VotingCount> getVotes(String rule_id);
	
	Vote updateVote(Vote v);
	
	Vote getVote(String rule_id, String ip, String field);

}
