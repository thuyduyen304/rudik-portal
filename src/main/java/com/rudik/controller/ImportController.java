package com.rudik.controller;

//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.attribute.standard.PrinterLocation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.rudik.model.Rule;

@Controller
public class ImportController {
   
    @Autowired
    RuleRepository ruleRepository;
    
    private KBPredicateSelector kbAnalysis;
    private DynamicPruningRuleDiscovery naive;
    
    public ImportController() {
    	ConfigurationFacility.setConfigurationFile("src/main/resources/DbpediaConfiguration.xml");
    	kbAnalysis = new SparqlKBPredicateSelector();
    	naive = new DynamicPruningRuleDiscovery();
    }
    
    @GetMapping("/rule/import")
    public String showImportForm(Model model) {
    	Map<String, String> knowledgeBases = new HashMap<String, String>() {{
        	put("dbpedia", "DBpedia");
            put("yago", "Yago");
            put("wikidata", "Wikidata");
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
    public String importRules(@RequestParam("file") MultipartFile file, 
    		@RequestParam String knowledgeBase, @RequestParam String source, RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:showImportForm";
        }

        try {
//        	String t_pred = "http://dbpedia.org/ontology/launchSite";
//        	String t_prem = "http://dbpedia.org/ontology/country(object,v0) & http://dbpedia.org/ontology/countryOrigin(subject,v0)";
////        	Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(t_pred);
//	        String typeSubject = "http://dbpedia.org/ontology/Rocket";
//	        String typeObject = "http://dbpedia.org/ontology/Place";
//	        Set<String> set_relations = Sets.newHashSet(t_pred);
//	        Set<RuleAtom> rule_atom = HornRule.readHornRule(t_prem);
//	        Double score = naive.getRuleConfidence(rule_atom, set_relations, typeSubject, typeObject, true);
//	        System.out.println("test:" + score);
////	        rule.setComputedConfidence(score);
////			ruleRepository.save(rule);
        	
        	     	
        	InputStreamReader reader = new InputStreamReader(file.getInputStream());
//        	CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
//        	CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
//        	String fileName = "C:\\Users\\DzienDzien\\Downloads\\dbpedia38.csv";
//        	FileReader reader = new FileReader(fileName);
        	CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(reader);  
        	
        	for (CSVRecord record : csvParser) {
        		String rule_string = record.get("Rule");
        		try {
					Rule rule = Parser.amie_to_rudik("dbpedia", rule_string);
					System.out.println("test hash:" + rule.hashCode());
					List<Rule> check_exist = ruleRepository.findByHashcode(rule.hashCode());
					if (check_exist.size() > 0) {
						//rule exist
					} else {	        
						Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(rule.getPredicate());
				        String typeSubject = subjectObjectType.getLeft();
				        String typeObject = subjectObjectType.getRight();
//				        System.out.println("test sbj:" + typeSubject);
//				        System.out.println("test obj:" + typeObject);
//				        System.out.println("test predicate:" + rule.getPredicate());
//				        System.out.println("test premise:" + rule.getPremise());
//				        System.out.println("test type:" + rule.getRuleType());
				        Set<String> set_relations = Sets.newHashSet(rule.getPredicate());
				        Set<RuleAtom> rule_atom = HornRule.readHornRule(rule.getPremise());
				        Double score = naive.getRuleConfidence(rule_atom, set_relations, typeSubject, typeObject, rule.getRuleType());
				        System.out.println("test score:" + score);
				        rule.setComputedConfidence(score);
						ruleRepository.save(rule);
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }
}
