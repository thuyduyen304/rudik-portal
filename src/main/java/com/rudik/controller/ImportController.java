package com.rudik.controller;

//import java.io.FileReader;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rudik.utils.Parser;
import com.rudik.utils.RuleMiningSystem;

import com.google.gson.Gson;
import com.rudik.dal.RuleDALImpl;
import com.rudik.model.Atom;
import com.rudik.model.Rule;

@Controller
public class ImportController {

    RuleDALImpl ruleRepository;
    RuleMiningSystem miningSystem;
    
    public ImportController(RuleDALImpl ruleRepository, RuleMiningSystem miningSystem) {
    	this.ruleRepository = ruleRepository;
    	this.miningSystem = miningSystem;
    }
    
    @GetMapping("/rules/import")
    @Secured({"ROLE_ADMIN"})
    public String showImportForm(Model model) {
    	Map<String, String> methods = new HashMap<String, String>() {{
        	put("all", "Any rules");
            put("amie", "AMIE rules on DBpedia");
            put("rudik", "RUDIK rules on DBpedia");
        }};
        
        model.addAttribute("methods", methods);
        model.addAttribute("page", "admin");
        
        return "rule/import";
    }
    
    @PostMapping("/rules/import") 
    @Secured({"ROLE_ADMIN"})
    public String importRules(@RequestParam("file") MultipartFile file, 
    		@RequestParam String method, RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:/rule/import";
        }
        String msg = "";
        int count_total = 0;
        int count_success = 0;
        int count_existing = 0;
        int count_data_error = 0;
        String knowledgeBase = "dbpedia";
        

        try {
        	InputStreamReader reader = new InputStreamReader(file.getInputStream());
        	CSVParser csvParser;
        	
        	switch (method) {
        		case "amie":
                	csvParser = CSVFormat.DEFAULT.withHeader().parse(reader);  
                	
                	for (CSVRecord record : csvParser) {
                		if(record.isSet("rule")) {
                			String rule_string = record.get("rule");
                    		try {
            					Rule rule = Parser.amie_to_rudik(knowledgeBase, rule_string);
            					count_total++;
            					Rule check_exist = ruleRepository.getRuleByHashcode(rule.hashCode());
            					if (check_exist != null) {
            						//rule exist
            						count_existing++;
            						if (!check_exist.getSource().contains("amie")) {
            							check_exist.addSource("amie");
            							ruleRepository.saveRule(check_exist);
            						}
            					} else {
            						rule.addSource("amie");
            						Double score;
            						try {
            							score = Double.parseDouble(record.get("support"));
            						} catch (Exception e) {
            							score = miningSystem.getScore(rule);
            						}
            						rule.setStatus(true);
            				        rule.setComputed_confidence(score);
            				        rule.setHashcode(rule.hashCode());
            						ruleRepository.saveRule(rule);
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
                			count_total++;
                			String predicate = record.get("relation").trim();
                    		Boolean type = record.get("type").equals("1");
                    		String premise = record.get("premise").trim();
                    		
                    		Set<Atom> premise_triples = Parser.premise_to_atom_list(premise);
                    		
                    		if(premise_triples.size() > 0) {
                        		Rule rule = new Rule();
                    			rule.setPremise(premise);
                    			rule.setPredicate(predicate);
                    			rule.setRule_type(type);
                    			rule.setKnowledge_base(knowledgeBase);
                    			rule.setPremise_triples(premise_triples);
                    			rule.setStatus(true);
                    			Atom conclusion = new Atom("subject", predicate, "object");
                        		rule.setConclusion_triple(conclusion);
                        		rule.setConclusion(conclusion.toString());

            					Rule check_exist = ruleRepository.getRuleByHashcode(rule.hashCode());
            					if (check_exist != null) {
            						//rule exist
            						count_existing++;
            						if (!check_exist.getSource().contains("rudik")) {
            							check_exist.addSource("rudik");
            							ruleRepository.saveRule(check_exist);
            						}
            					} else {
            						rule.addSource("rudik");
            						Double score;
                            		try {
                            			score = Double.parseDouble(record.get("support"));
                            		} catch(Exception e) {
                            			score = miningSystem.getScore(rule);
                            		}
                        			rule.setComputed_confidence(score);
            						rule.setHashcode(rule.hashCode());
            						ruleRepository.saveRule(rule);
            						count_success++;
            					}
                    		} else {
                    			count_data_error++;
                    		}
                    		
                		}
                		
                    }
        			break;
        		case "all":
        			String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        			if (ext.equals("json")) {
        				try {
        					Gson gson = new Gson();
        					Rule[] rules = gson.fromJson(reader, Rule[].class);
        					count_total = rules.length;
        					
        					for(Rule rule: rules) {
        						if (rule.valid() == false) {
        							count_data_error++;
        						} else {
        							Rule check_exist = ruleRepository.getRuleByHashcode(rule.hashCode());
                					if (check_exist != null) {
                						//rule exist
                						count_existing++;
                						boolean changed = false;
                						if (rule.getSource() != null) {
                							for (String source: rule.getSource()) {
                    							if (!check_exist.getSource().contains(source)) {
                    								check_exist.addSource(source);
                    								changed = true;
                    							}
                    						}
                						}
                						if (changed) {
                							ruleRepository.saveRule(check_exist);
                						}
                					} else {
            							rule.setRuleId(null);
            							try {
            								if (rule.getPremise_triples() == null) {
                    							rule.setPremise_triples(Parser.premise_to_atom_list(rule.getPremise()));
                    						}
                							if (rule.getConclusion_triple() == null) {
                    							rule.setConclusion_triple(Parser.str_to_atom(rule.getConclusion()));
                    						}
                							rule.setHashcode(rule.hashCode());
                    						rule.setStatus(true);
                    						ruleRepository.saveRule(rule);
                    						count_success++;
            							} catch(Exception e) {
            								count_data_error++;
            							}
                					}
        						}
        						
        					}
        				} catch (Exception e) {
        					System.out.println(e);
        				}
        				
        			}
        			break;
        	}
        } catch (Exception e) {
            e.printStackTrace();
            msg = "Error. " + e.getMessage();
        }
        msg = msg + "\nFound " + count_total + " rules. " + "Successfully imported " + count_success + " rule(s). \n" + 
        "Ignored " + count_existing + " existing rule(s) and " + count_data_error + " invalid rule(s)";
        redirectAttributes.addFlashAttribute("msg", msg);

        return "redirect:/rules/manage";
    }
}
