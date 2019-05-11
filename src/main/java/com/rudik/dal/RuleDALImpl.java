package com.rudik.dal;

import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.rudik.model.Rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RuleDALImpl implements RuleDAL {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Rule> getAllRules() {
		return mongoTemplate.findAll(Rule.class);
	}

	@Override
	public Rule getRuleById(String ruleId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("ruleId").is(ruleId));
		return mongoTemplate.findOne(query, Rule.class);
	}

	@Override
	public Rule addNewRule(Rule rule) {
		mongoTemplate.save(rule);
		return rule;
	}

	@Override
	public List<String> getAllKnowledgeBase() {
		@SuppressWarnings("unchecked")
		List<String> kb = mongoTemplate.getCollection("rules").distinct("knowledge_base");
		return kb;
	}

	@Override
	public List<Rule> getRuleByKnowledgeBase(String knowledge_base) {
		// TODO Auto-generated method stub
		Query query = new Query();
		query.addCriteria(Criteria.where("knowledgeBase").is(knowledge_base));
		return mongoTemplate.find(query, Rule.class);
	}

	@Override
	public List<String> getAllPredicates(String knowledge_base) {
		// TODO Auto-generated method stub
		BasicDBObject o = new BasicDBObject("knowledge_base", "DBPedia");
		@SuppressWarnings("unchecked")
		List<String> predicates = mongoTemplate.getCollection("rules").distinct("predicate");
		return predicates;
	}
	
	@Override
	public List<Rule> getRulesByCriteria(List<HashMap<String, Object>> criteria) {
		Query query = new Query();
		for(HashMap<String, Object> criterion : criteria) {
			String field = (String)criterion.get("field");
			String op = (String)criterion.get("op");
			Object value = criterion.get("value");
			switch(op) {
			case "=":
				query.addCriteria(Criteria.where(field).is(value));
				break;
			case ">":
				query.addCriteria(Criteria.where(field).gt(value));
				break;
			case "<":
				query.addCriteria(Criteria.where(field).lt(value));
				break;
			case "BETWEEN":
				Object[] values = (Object[]) value;
				query.addCriteria(Criteria.where(field).gte(values[0]).lte(values[1]));
				break;
			case "NOT_NULL":
				query.addCriteria(Criteria.where(field).ne(null));
				break;
			}
			
		}
		System.out.println(query); 
		return mongoTemplate.find(query, Rule.class);
	}

	@Override
	public Rule updateQualityEvaluation(String rule_id, Rule changes) {
		
		Query query = new Query();
		query.addCriteria(Criteria.where("ruleId").is(rule_id));
		Rule rule = mongoTemplate.findOne(query, Rule.class);
		rule.setQualityEvaluation(changes.getQualityEvaluation());
		mongoTemplate.save(rule);
		return rule;
	}
	
	@Override
	public Rule updateStatus(String rule_id, Boolean changes) {
		
		Query query = new Query();
		query.addCriteria(Criteria.where("ruleId").is(rule_id));
		Rule rule = mongoTemplate.findOne(query, Rule.class);
		rule.setStatus(changes);
		mongoTemplate.save(rule);
		return rule;
	}

	
}
