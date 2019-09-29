package com.rudik.dal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.rudik.model.Rule;
import com.rudik.model.Vote;
import com.rudik.model.VotingCount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
		List<String> kb = new ArrayList<String>();
	    DistinctIterable<String> distinctIterable = this.mongoTemplate.getCollection("rules").distinct("knowledge_base", String.class);
	    MongoCursor<String> cursor = distinctIterable.iterator();
	    while (cursor.hasNext()) {
	      String item = (String)cursor.next();
	      kb.add(item);
	    } 
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
		if (knowledge_base == "yago3")
	    	  knowledge_base = "yago";
		BasicDBObject o = new BasicDBObject("knowledge_base", "dbpedia");
		List<String> predicates = new ArrayList<String>();
	    DistinctIterable<String> distinctIterable = this.mongoTemplate.getCollection("rules").distinct("predicate", String.class);
	    MongoCursor<String> cursor = distinctIterable.iterator();
	    while (cursor.hasNext()) {
	      String item = (String)cursor.next();
	      if(item.contains(knowledge_base))
	    	  predicates.add(item);
	    } 
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
//		query.addCriteria(Criteria.where("knowledge_base").is("dbpedia"));
		System.out.println(query); 
		List<Rule> test = mongoTemplate.find(query, Rule.class);
		System.out.println(test);
		return test;
	}
	
	@Override
	public List<Rule> getAllValidRules() {
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is(true));
		return mongoTemplate.find(query, Rule.class);
	}

	@Override
	public Rule updateQualityEvaluation(String rule_id, Rule changes) {
		
		Query query = new Query();
		query.addCriteria(Criteria.where("ruleId").is(rule_id));
		Rule rule = mongoTemplate.findOne(query, Rule.class);
		if(changes.getQuality_evaluation() != null)
			rule.setQuality_evaluation(changes.getQuality_evaluation());
		if(changes.getHuman_confidence() != null)
			rule.setHuman_confidence(changes.getHuman_confidence());
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

	@Override
	public List<VotingCount> getVotes(String ruleId) {
	    Aggregation agg = Aggregation.newAggregation(new AggregationOperation[] {
	          Aggregation.match(Criteria.where("ruleId").is(ruleId)), 
	          Aggregation.match(Criteria.where("field").is("quality_evaluation")), 
	          Aggregation.group(new String[] { "rating" }).count().as("total"), 
	          Aggregation.project(new String[] { "total" }).and("rating").previousOperation()
	        });
	    
	    AggregationResults<VotingCount> group_results = this.mongoTemplate.aggregate(agg, Vote.class, VotingCount.class);
	    
	    List<VotingCount> result = group_results.getMappedResults();
	    
	    return result;
	}
	
	@Override
	public Vote updateVote(Vote vote) {
	    this.mongoTemplate.save(vote);
	    return vote;
	}
	
	@Override
	public Vote getVote(String rule_id, String ip, String field) {
	    Query query = new Query();
	    query.addCriteria(Criteria.where("ruleId").is(rule_id));
	    query.addCriteria(Criteria.where("ip").is(ip));
	    query.addCriteria(Criteria.where("field").is(field));
	    return (Vote)this.mongoTemplate.findOne(query, Vote.class);
	}
	
	@Override
	public Rule updateRule(Rule rule) {
	    this.mongoTemplate.save(rule);
	    return rule;
	}
}
