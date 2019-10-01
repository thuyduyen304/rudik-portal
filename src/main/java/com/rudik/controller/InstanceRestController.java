package com.rudik.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rudik.dal.InstanceDAL;
import com.rudik.dal.RuleDAL;
import com.rudik.model.Atom;
import com.rudik.model.Instance;
import com.rudik.model.Rule;
import com.rudik.utils.Utils;

import asu.edu.rule_miner.rudik.api.RudikApi;
import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;

@RestController
@RequestMapping(value = "/api/instances")
public class InstanceRestController {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final InstanceDAL instanceDAL;
	private final RuleDAL ruleDAL;
	
	private String dbpediaConfig;
	
	private String yagoConfig;
	
	private Integer maxInstances = 5000;

	public InstanceRestController(@Value("${app.rudikDbpediaConfig}") String dbpediaConfig, 
			@Value("${app.rudikYagoConfig}") String yagoConfig, @Value("${app.rudikMaxInst}") int maxInst, 
			InstanceDAL InstanceDAL, RuleDAL RuleDAL) {
		this.instanceDAL = InstanceDAL;
		this.ruleDAL = RuleDAL;
		this.maxInstances = maxInst;
		this.dbpediaConfig = dbpediaConfig;
		this.yagoConfig = yagoConfig;
	}
	
	@RequestMapping("/rule-samples/{id}")
	public List<Instance> sampleRule(@PathVariable(value="id") String rule_id) {
		Rule rule = ruleDAL.getRuleById(rule_id);
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id, "sample_instances");
		if(instances.size() == 0) {
			String rudikConfig = "";
			// get from dbpedia or yago
			if (rule.getKnowledge_base().equals("dbpedia"))
				rudikConfig = dbpediaConfig;
			else if (rule.getKnowledge_base().equals("yago3"))
				rudikConfig = yagoConfig;
			Map<String, List<RuleAtom>> rulesAtomsDict = new HashMap<>();
	        Map<String, String> rules_entities_dict = new HashMap<>();
	        List<String> returnResult = new ArrayList<>();
	        
			HornRule horn_rule = HornRule.createHornRule(rule.getPremise());
			String predicate = rule.getPredicate();
			HornRuleResult.RuleType type = rule.getRule_type() ? HornRuleResult.RuleType.positive : HornRuleResult.RuleType.negative;
			RudikApi API = new RudikApi(rudikConfig,
                    5 * 60,
                    false,
                    maxInstances);
			
			final RudikResult result = API.instantiateSingleRule(horn_rule, predicate, type, 20);
            if (result != null) {
            	for (final HornRuleResult oneResult : result.getResults()) {
            		Random rand = new Random();
            		// get all instantiation of the rule over the KB
                    final List<HornRuleInstantiation> ruleInst = oneResult.getAllInstantiations();
                	
                	for (HornRuleInstantiation instance : ruleInst) {
                        
                        String sep = "";
                        String entitieSep = "";
                        String temp = "";
                        String ruleEntities = "";
                        //store the previous atom to compare with the new one and remove the instantiation if two following atoms are the same
                        List<String> ruleAtoms = new ArrayList<String>();
                        int i = 0;
                        List<RuleAtom> ruleAtomsList = new LinkedList<>();

                        // iterate over all instantiated atoms of the rule
                        Document assignment = new Document();
                        
                        assignment.append("subject", instance.getRuleSubject());
                        assignment.append("object", instance.getRuleObject());
                        for (final RuleAtom atom : instance.getInstantiatedAtoms()) {
                            //list of atoms composing a rule

                            File relation = new File(atom.getRelation());
                            //if the current atoms is already in the list of atoms set i to 1
                            String constructed_name = relation.getName() + "(" + atom.getSubject() + "," + atom.getObject() + ")";
                            if (Utils.is_in_list(ruleAtoms, constructed_name)) {
                                i++;
                            }
                            temp += sep + relation.getName() + "(" + atom.getSubject() + "," + atom.getObject() + ")";
                            sep = " & ";
                            ruleAtoms.add(relation.getName() + "(" + atom.getSubject() + "," + atom.getObject() + ")");
                            ruleAtomsList.add(atom);
                            //construct the string of entities that compose the instantiated rule
                            if (!ruleEntities.contains(atom.getSubject())) {
                                ruleEntities += entitieSep + atom.getSubject();
                                entitieSep = ";";
                            }
                            if (!ruleEntities.contains(atom.getObject())) {
                                ruleEntities += entitieSep + atom.getObject();
                                entitieSep = ";";

                            }
                        }
                        rulesAtomsDict.put(temp, ruleAtomsList);
                        rules_entities_dict.put(temp, ruleEntities);
                        if (!Utils.is_in_list(returnResult, temp) & i == 0) {
                            returnResult.add(temp);
                            
                            Instance inst = new Instance();
                            inst.setRuleId(rule.getRuleId());
                            inst.setPredicate(predicate);

                            List<Atom> instance_atoms = new LinkedList<>();
                            
                            StringBuilder premise = new StringBuilder();
                            for (RuleAtom atom : ruleAtomsList) {
                                // build rule entities
                            	instance_atoms.add(new Atom(atom.getSubject(), atom.getRelation(), atom.getObject()));
                                if (!premise.toString().equals("")) {
                                    premise.append(" & ").append(atom.getRelation()).append("(").append(atom.getSubject()).append(",").append(atom.getObject()).append(")");
                                } else {
                                    premise.append(atom.getRelation()).append("(").append(atom.getSubject()).append(",").append(atom.getObject()).append(")");
                                }
                            }
                            
                            inst.setPremiseTriples(instance_atoms);

                            // build conclusion
                            StringBuilder conclusion = new StringBuilder();
                            Atom conclusion_triple = new Atom(assignment.getString("subject"), predicate, assignment.getString("object"));

                            conclusion.append(predicate).append("(").append(assignment.get("subject")).append(",").append(assignment.get("object")).append(")");

                            inst.setConclusion(conclusion.toString());
                            inst.setConclusionTriple(conclusion_triple);
                            inst.setHashcode(inst.hashCode());
                            inst.setLabel((short) -1);
                            inst.setPremise(premise.toString());
                            inst.setRuleType(rule.getRule_type());
                            
                            instanceDAL.addNewInstance(inst, "sample_instances");
                            instances.add(inst);

                        }
                	}
            	}
          
            }
		}
		return instances;
	}
	
	@PutMapping("/{id}")
	public Instance updateLabel(@RequestBody Instance changes, @PathVariable(value="id") String id) {
		Instance instance = new Instance();
		instance = instanceDAL.updateLabel(id, changes);

		return instance;
	}
	
  @PutMapping("/compute_hc/{id}")
	public Double computeHumanConfidence(Rule changes, @PathVariable(value="id") String id) {
		Rule rule = new Rule();
		List<Instance> instances = instanceDAL.getInstanceByRuleId(id, "sample_instances");
		Double hc = 0.;
		for (Instance instance : instances)  
    { 
			hc += instance.getLabel();
    } 
		hc = hc / 20;
		changes.setHuman_confidence(hc);
		rule = ruleDAL.updateQualityEvaluation(id, changes);

		return hc;
	}
  
  	@Secured({"ROLE_ADMIN"})
	@RequestMapping({"/update_hashcode"})
  	public String updateHashcode() {
		List<Instance> insts = instanceDAL.getAllInstances();
		int j = 0;
		for(Instance i: insts) {
			Integer new_hashcode = i.hashCode();
			i.setHashcode(new_hashcode);
			instanceDAL.updateInstance(i);
			j++;
		}
		
		return "done for " + j + " rules";
  	}
	
}
