package com.rudik.dal;

import java.util.List;

import com.rudik.model.Instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class InstanceDALImpl implements InstanceDAL {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Instance> getAllInstances() {
		return mongoTemplate.findAll(Instance.class);
	}

	@Override
	public Instance getInstanceById(String instanceId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("instanceId").is(instanceId));
		return mongoTemplate.findOne(query, Instance.class);
	}

	@Override
	public List<Instance> getInstanceByRuleId(String rule_id, String collection) {
		Query query = new Query();
		query.addCriteria(Criteria.where("ruleId").is(rule_id));
		return mongoTemplate.find(query, Instance.class, collection);
	}
	
	
}
