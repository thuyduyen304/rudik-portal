package com.rudik.dal;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.rudik.model.Rule;

@Repository
public interface RuleRepository extends MongoRepository<Rule, String> {
	List<Rule> findByHashcode(Integer hashcode);
}

