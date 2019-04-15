package com.rudik.dal;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.rudik.model.Instance;
import com.rudik.model.Rule;

@Repository
public interface InstanceRepository extends MongoRepository<Instance, String> {
}

