package com.rudik.dal;

import java.util.List;

import com.rudik.model.Instance;

public interface InstanceDAL {
	List<Instance> getAllInstances();

	Instance getInstanceById(String instanceId);
	
	List<Instance> getInstanceByRuleId(String rule_id);
	
	Instance addNewInstance(Instance inst);
	
	Instance updateLabel(String instance_id, Instance changes);
	
	Instance updateInstance(Instance inst);

}
