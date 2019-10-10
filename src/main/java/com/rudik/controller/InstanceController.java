package com.rudik.controller;



import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rudik.dal.InstanceDAL;
import com.rudik.dal.InstanceRepository;
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
//import static asu.edu.rule_miner.rudikUI.DiscoverNewRules.isInList;

@Controller
@RequestMapping(value = "/instance")
public class InstanceController {
	private final InstanceRepository instanceRepository;

	private final InstanceDAL instanceDAL;
	
	private RuleDAL ruleDAL;
	
	@Value("${app.rudikYagoConfig}")
	private String yagoConfig;
	
	@Value("${app.rudikDbpediaConfig}")
	private String dbpediaConfig;
	
	@Value("${app.rudikWikidataConfig}")
	private String wikidataConfig;
	
	@Value("${app.rudikMaxInst}")
	private Integer maxInstances;

	public InstanceController(
			InstanceRepository instanceRepository, InstanceDAL instanceDAL,
			RuleDAL ruleDAL) {
		this.instanceRepository = instanceRepository;
		this.instanceDAL = instanceDAL;
		this.ruleDAL = ruleDAL;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/sample")
	public void export(@RequestParam(name = "rule_id", required = false, defaultValue = "a") String rule_id,
			HttpServletResponse response) throws IOException {
		Rule rule = ruleDAL.getRuleById(rule_id);
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id, "sample_instances");
		
		if(instances.size() == 0) {
			String rudikConfig = Utils.get_config_from_kb(rule.getKnowledge_base(), yagoConfig, dbpediaConfig, wikidataConfig);
			
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
                    
//                	int number_of_samples = ruleInst.size() > 20 ? 20 : ruleInst.size();
                	
                	for (HornRuleInstantiation instance : ruleInst) {
//                	for (int j = 0; j < number_of_samples; j++) {
//                        int randomIndex = rand.nextInt(ruleInst.size());
//                        HornRuleInstantiation instance = ruleInst.get(randomIndex);
//                        ruleInst.remove(randomIndex);
                        
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
		
		ObjectWriter mapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String myString = mapper.writeValueAsString(instances);

		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment;filename=instances_" + rule_id + ".json");

		PrintWriter out = response.getWriter(); 
		out.println(myString);
		out.flush();
		out.close();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/all")
	public void export_all(@RequestParam(name = "rule_id", required = false, defaultValue = "a") String rule_id,
			HttpServletResponse response) throws IOException {
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id, "instances");
		ObjectWriter mapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String myString = mapper.writeValueAsString(instances);

		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment;filename=instances_" + rule_id + ".json");
		PrintWriter out = response.getWriter(); 
		out.println(myString);
		out.flush();
		out.close();
	}

}
