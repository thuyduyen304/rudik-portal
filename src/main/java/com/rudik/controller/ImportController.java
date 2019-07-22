package com.rudik.controller;

//import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rudik.utils.Parser;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.predicate.analysis.KBPredicateSelector;
import asu.edu.rule_miner.rudik.predicate.analysis.SparqlKBPredicateSelector;
import asu.edu.rule_miner.rudik.rule_generator.DynamicPruningRuleDiscovery;

import com.rudik.dal.RuleRepository;
import com.rudik.model.Atom;
import com.rudik.model.Rule;

@Controller
public class ImportController {
   
    @Autowired
    RuleRepository ruleRepository;
    
    private KBPredicateSelector kbAnalysis;
    private DynamicPruningRuleDiscovery naive;
    
    public ImportController(@Value("${app.rudikConfig}") String config) {
    	ConfigurationFacility.setConfigurationFile(config);
    	kbAnalysis = new SparqlKBPredicateSelector();
    	naive = new DynamicPruningRuleDiscovery();
    }
    
    @GetMapping("/rule/import")
    @Secured({"ROLE_ADMIN"})
    public String showImportForm(Model model) {
    	Map<String, String> knowledgeBases = new HashMap<String, String>() {{
        	put("dbpedia", "DBpedia");
//            put("yago", "Yago");
//            put("wikidata", "Wikidata");
        }};
        
        Map<String, String> sources = new HashMap<String, String>() {{
        	put("rudik", "Rudik");
            put("amie", "Amie");
        }};
        
    	
        model.addAttribute("knowledgeBases", knowledgeBases);
        model.addAttribute("sources", sources);
        
        return "rule/import";
    }
    
    @PostMapping("/rule/import") 
    @Secured({"ROLE_ADMIN"})
    public String importRules(@RequestParam("file") MultipartFile file, 
    		@RequestParam String knowledgeBase, @RequestParam String source, RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:/rule/import";
        }
        String msg = "";
        int count_success = 0;
        int count_existing = 0;

        try {
        	InputStreamReader reader = new InputStreamReader(file.getInputStream());
        	CSVParser csvParser;
        	
        	switch (source) {
        		case "amie":
                	csvParser = CSVFormat.DEFAULT.withHeader().parse(reader);  
                	
                	for (CSVRecord record : csvParser) {
                		if(record.isSet("rule")) {
                			String rule_string = record.get("rule");
                    		try {
            					Rule rule = Parser.amie_to_rudik(knowledgeBase, rule_string);
            					List<Rule> check_exist = ruleRepository.findByHashcode(rule.hashCode());
            					if (check_exist.size() > 0) {
            						//rule exist
            						count_existing++;
            					} else {
            						Double support;
            						try {
            							support = Double.parseDouble(record.get("support"));
            						} catch (Exception e) {
            							Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(rule.getPredicate());
                				        String typeSubject = subjectObjectType.getLeft();
                				        String typeObject = subjectObjectType.getRight();
                				        Set<String> set_relations = Sets.newHashSet(rule.getPredicate());
                				        Set<RuleAtom> rule_atom = HornRule.readHornRule(rule.getPremise());
                				        support = naive.getRuleConfidence(rule_atom, set_relations, typeSubject, typeObject, rule.getRuleType());
            						}
            						rule.setStatus(true);
            				        rule.setComputedConfidence(support);
            				        rule.setHashcode(rule.hashCode());
            						ruleRepository.save(rule);
            						count_success++;
            					}
            					
            				} catch (Exception e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            					msg += "Error. Cannot import rules";
            				}
                		}
                    }
        			break;
        		case "rudik":
                	csvParser = CSVFormat.newFormat(';').withHeader()
                			.withIgnoreSurroundingSpaces().parse(reader);  
                	
                	for (CSVRecord record : csvParser) {
                		if (record.isSet("relation") &&
                				record.isSet("type") &&
                				record.isSet("premise")) {
                			String predicate = record.get("relation").trim();
                    		Boolean type = record.get("type").equals("1");
                    		String premise = record.get("premise").trim();
                    		
                    		Set<Atom> premise_triples = Parser.premise_to_atom_list(premise);
                    		
                    		if(premise_triples.size() > 0) {
                    			Double support;
                        		try {
                        			support = Double.parseDouble(record.get("support"));
                        		} catch(Exception e) {
                        			Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(predicate);
            				        String typeSubject = subjectObjectType.getLeft();
            				        String typeObject = subjectObjectType.getRight();
            				        Set<String> set_relations = Sets.newHashSet(predicate);
            				        Set<RuleAtom> rule_atom = HornRule.readHornRule(premise);
            				        support = naive.getRuleConfidence(rule_atom, set_relations, typeSubject, typeObject, type);
                        		}
                        		Rule rule = new Rule("rudik");
                    			rule.setPremise(premise);
                    			rule.setPredicate(predicate);
                    			rule.setRuleType(type);
                    			rule.setStatus(true);
                    			rule.setComputedConfidence(support);
                    			rule.setKnowledgeBase(knowledgeBase);
                    			rule.setPremiseTriples(premise_triples);
                    			rule.setStatus(true);
                    			Atom conclusion = new Atom("subject", predicate, "object");
                        		rule.setConclusionTriple(conclusion);
                        		rule.setConclusion(conclusion.toString());

            					List<Rule> check_exist = ruleRepository.findByHashcode(rule.hashCode());
            					if (check_exist.size() > 0) {
            						//rule exist
            						count_existing++;
            					} else {	        
            						rule.setHashcode(rule.hashCode());
            						ruleRepository.save(rule);
            						count_success++;
            					}
                    		}
                    		
                		}
                		
                    }
        			break;
        	}
        		


        } catch (Exception e) {
            e.printStackTrace();
        }
        redirectAttributes.addFlashAttribute("msg", msg + "Successfully imported " + count_success + " rules. \n" + 
                "Ignored " + count_existing + " rules.");

        return "redirect:/";
    }
}
