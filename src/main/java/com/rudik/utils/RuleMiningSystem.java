package com.rudik.utils;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rudik.model.Atom;
import com.rudik.model.Instance;
import com.rudik.model.Rule;

import asu.edu.rule_miner.rudik.api.RudikApi;
import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;

@Component
public class RuleMiningSystem {
	@Value("${app.rudikYagoConfig}")
	private String yagoConfig;
	
	@Value("${app.rudikDbpediaConfig}")
	private String dbpediaConfig;
	
	@Value("${app.rudikWikidataConfig}")
	private String wikidataConfig;
	
	@Value("${app.rudikMaxInst}")
	private Integer maxInstances;
	
	private static RudikApi API;
	
	private void initAPI(String kb) {
		String config = this.dbpediaConfig;
		if (kb.equals(Utils.YAGO3)) {
			config = this.yagoConfig;
		} else if (kb.equals(Utils.WIKIDATA)) {
			config = this.wikidataConfig;
		}
		if (API == null) {
			API = new RudikApi(config,
	                5 * 60,
	                false,
	                maxInstances);
		} else {
			System.out.print("Rudik API is instantiated already.");
			ConfigurationFacility.setConfigurationFile(config);
		}
	}
	
	public List<Instance> getInstances(Rule rule) {
		// check the kb of rule, init the proper config or change the config
		this.initAPI(rule.getKnowledge_base());
		
		// get instances
		List<Integer> instances_hashcode = new ArrayList<>();
		List<Instance> instances = new ArrayList<>();
		
		HornRule horn_rule = HornRule.createHornRule(rule.getPremise());
		String predicate = rule.getPredicate();
		HornRuleResult.RuleType type = rule.getRule_type() ? HornRuleResult.RuleType.positive : HornRuleResult.RuleType.negative;

		final RudikResult result = RuleMiningSystem.API.instantiateSingleRule(horn_rule, predicate, type, 20);
		if (result != null) {
        	for (final HornRuleResult oneResult : result.getResults()) {
        		// get all instantiation of the rule over the KB
                final List<HornRuleInstantiation> ruleInst = oneResult.getAllInstantiations();
            	
            	for (HornRuleInstantiation instance : ruleInst) {
                    Instance inst = new Instance();
                    inst.setRuleId(rule.getRuleId());
                    inst.setPredicate(predicate);
                    
                    Set<Atom> instance_atoms = new HashSet<Atom>();
                    
                    for (final RuleAtom atom : instance.getInstantiatedAtoms()) {
                    	instance_atoms.add(new Atom(atom.getSubject(), atom.getRelation(), atom.getObject()));
                    }
                    
                    inst.setPremiseTriples(instance_atoms);
                    inst.setPremise(Parser.atoms_to_str(instance_atoms));
                    
                    Atom conclusion_atom = new Atom(instance.getRuleSubject(), predicate, instance.getRuleObject());
                    inst.setConclusionTriple(conclusion_atom);
                    inst.setConclusion(conclusion_atom.toString());

                    inst.setLabel((short) -1);
                    inst.setRuleType(rule.getRule_type());
                    inst.setHashcode(inst.hashCode());
                    
                    if (!instances_hashcode.contains(inst.getHashcode())) {
                    	instances.add(inst);
                    	instances_hashcode.add(inst.getHashcode());
                    }
            	}
        	}
        }
		return instances;
	}
	
	public String getSparqlQuery(Rule rule) {
		this.initAPI(rule.getKnowledge_base());
		String query = API.getSparqlQuery(rule.getPremise(), rule.getPredicate(), rule.getRule_type());
		return query;
	}
	
	public Double getScore(Rule rule) throws Exception {
		this.initAPI(rule.getKnowledge_base());
		Double score = API.getRuleConfidence(rule.getPremise(), rule.getPredicate(), rule.getRule_type());
		return score;
	}
}
