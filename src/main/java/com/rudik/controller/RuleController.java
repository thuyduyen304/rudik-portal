package com.rudik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.rudik.dal.RuleDAL;
import com.rudik.dal.InstanceDAL;
import com.rudik.model.Atom;
import com.rudik.model.Instance;
import com.rudik.model.Rule;
import com.rudik.utils.Parser;
import com.rudik.utils.RuleMiningSystem;
import com.rudik.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

@Controller
public class RuleController {
	private RuleDAL ruleDAL;
	private InstanceDAL instanceDAL;
	private RuleMiningSystem miningSystem;
	
	public RuleController(InstanceDAL instanceDAL,
			RuleDAL ruleDAL, RuleMiningSystem miningSystem) {
		this.instanceDAL = instanceDAL;
		this.ruleDAL = ruleDAL;
		this.miningSystem = miningSystem;
	}
    
    @GetMapping(value = {"/", "/rules"})
    public String showSearchForm(Model model) {
    	Map<String, String> knowledge_bases = Utils.getKbs();
        model.addAttribute("knowledge_bases", knowledge_bases);

        Map<Integer, String> rule_types = Utils.getRuleTypes();
        model.addAttribute("rule_types", rule_types);
        
        model.addAttribute("page", "rules");
        
        return "rule/listRules";
    }
    
    
    @GetMapping(value = "/rules/add")
    public String showAddForm(Model model) {
    	model.addAttribute("knowledge_bases", Utils.getKbs());
    	
    	Map<Integer, String> rule_types = Utils.getRuleTypes();
    	
    	model.addAttribute("rule_types", rule_types);
    	model.addAttribute("page", "add-rules");
      
    	return "rule/addRules";
    }
    
    @Secured({"ROLE_ADMIN"})
    @GetMapping(value = "/rules/manage")
    public String approveForm(Model model) {
    	
		model.addAttribute("knowledgeBases", Utils.getKbs());

		Map<Integer, String> ruleTypes = new HashMap<Integer, String>() {{
			put(-1, "--None--");
			put(0, "Negative");
			put(1, "Positive");
		}};
		model.addAttribute("ruleTypes", ruleTypes);
	  
		Map<Integer, String> ruleStatus = new HashMap<Integer, String>() {{
			put(-1, "--None--");
			put(0, "Not Approved");
			put(1, "Approved");
		}};
	
		model.addAttribute("ruleStatus", ruleStatus);
		model.addAttribute("page", "admin");
	  
		return "rule/manageRules";
    }
    
    
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/rules/export-all", method = RequestMethod.GET)
    public  ResponseEntity<Resource> exportAllRules(@Value("${app.exportPath}") String directory) throws JsonIOException, IOException {
    	GsonBuilder builder = new GsonBuilder();
    	builder.serializeSpecialFloatingPointValues();
    	builder.setPrettyPrinting();
    	builder.disableHtmlEscaping();
        Gson gson = builder.create();
    	DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
    	Date date = new Date();
    	String file_path = directory + "/valid_rules_" + dateFormat.format(date) + ".json";
    	MediaType media_type = MediaType.APPLICATION_JSON_UTF8;
    	List<Rule> valid_rules = ruleDAL.getAllValidRules();
    	gson.toJson(valid_rules, new FileWriter(file_path));
    	//new OutputStreamWriter(new FileOutputStream(file_path), StandardCharsets.UTF_8)
    	File file = new File(file_path);
        InputStreamResource resource;
		resource = new InputStreamResource(new FileInputStream(file));
		
		return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                // Content-Type
                .contentType(media_type)
                // Content-Length
                .contentLength(file.length()) //
                .body(resource);
        
    }
    
    @GetMapping(value = "/rules/{id}/export-sample-instances")
	public void export(@RequestParam(name = "rule_id", required = false, defaultValue = "a") String rule_id,
			HttpServletResponse response) throws IOException {
		Rule rule = ruleDAL.getRuleById(rule_id);
		List<Instance> instances = instanceDAL.getInstanceByRuleId(rule_id);
		
		if(instances.size() == 0) {
			instances = miningSystem.getInstances(rule);
			for (Instance inst: instances) {
				instanceDAL.addNewInstance(inst);
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
    
    
    @RequestMapping(value = "/rules/{id}/sample-instances")
    public String sample_instances(Model model, @PathVariable(value="id") String rule_id) {
    	boolean isAdmin = false;
    	
    	try {
    		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    		for (GrantedAuthority au : user.getAuthorities()) {
            	if (au.getAuthority().equals("ROLE_ADMIN")) {
            		isAdmin=true;
            		break;
            	}
            }
    	} catch(Exception e) {
    		isAdmin=false;
    	}
        
    	Rule rule = ruleDAL.getRuleById(rule_id);
    	model.addAttribute("rule", rule);
    	model.addAttribute("page", "admin");
    	model.addAttribute("isAdmin", isAdmin);
    	return "rule/sampleInstances";
    } 
    
    @GetMapping(value = "/user-manual")
    public String info(Model model) {
    	model.addAttribute("page", "info");
    	return "rule/info";
    }
    
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(path = "/rule/import_pos_from_alldbpedia", method = RequestMethod.GET)
    public String importPosAllDbpedia(@Value("${app.alldbpedia_pos}") String path, ModelMap model) throws JsonIOException, IOException {
    	BufferedReader reader;
    	String predicate = "";
		Boolean output_flag = false;
		Boolean type = true;
		int count_existing = 0;
		int count_success = 0;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			
			while (line != null) {
				line = line.trim();
				if (line.length() > 0) {
					String predicate_pattern_str = "-------------------------\\[(\\D+)\\]";
		    		Pattern predicate_pattern = Pattern.compile(predicate_pattern_str);
		    		Matcher predicate_matcher = predicate_pattern.matcher(line);
		    		
		    		if(predicate_matcher.find()) {
		    			// new predicate
		    			predicate = predicate_matcher.group(1);
		    		} else {
		    			if (line.endsWith("--??FILLUP"))
		    				line = line.replace("--??FILLUP", "");
		    			
		    			// check if it's output
		    			if (line.equals("Output rules:")) {
		    				// multilines
		    				output_flag = true;
		    			} else if (line.startsWith("Output rules:")) {
		    				// one line
		    				output_flag = false;
		    				
		    				String output_pattern_str = "\\[((.+)\\((.+)\\,(.+)\\))\\]";
				    		Pattern output_pattern = Pattern.compile(output_pattern_str);
				    		Matcher output_matcher = output_pattern.matcher(line);
				    		
				    		if(output_matcher.find()) {
				    			// new predicate
				    			String output = output_matcher.group(1);
				    			// get rules
			    				String[] rules = output.split(",(?=(\\s|http://dbpedia.org/ontology/))");
			    				for(String premise : rules) {
			    					// create rule
			    					Set<Atom> premise_triples = Parser.premise_to_atom_list(premise);
			    					
			    					Rule rule = new Rule("rudik");
		                			rule.setPremise(premise);
		                			rule.setPredicate(predicate);
		                			rule.setRule_type(type);
		                			rule.setStatus(true);
		                			rule.setComputed_confidence(-1.);
		                			rule.setHuman_confidence(-1.);
		                			rule.setKnowledge_base("dbpedia");
		                			rule.setPremise_triples(premise_triples);
		                			Atom conclusion = new Atom("subject", predicate, "object");
		                    		rule.setConclusion_triple(conclusion);
		                    		rule.setConclusion(conclusion.toString());

		        					Rule check_exist = ruleDAL.getRuleByHashcode(rule.hashCode());
		        					if (check_exist != null) {
		        						//rule exist
		        						count_existing++;
		        					} else {	        
		        						rule.setHashcode(rule.hashCode());
		        						ruleDAL.saveRule(rule);
		        						count_success++;
		        					}
			    				}
				    		}
		    				
		    				
		    			} else if (output_flag) {
		    				// check if this line is a premise
		    	    		if(Parser.is_premise(line)) {
		    	    			// create a new rule
		    	    			String premise = line;
		    	    			Set<Atom> premise_triples = Parser.premise_to_atom_list(premise);
		    	    			
		    	    			Rule rule = new Rule("rudik");
	                			rule.setPremise(premise);
	                			rule.setPredicate(predicate);
	                			rule.setRule_type(type);
	                			rule.setStatus(true);
	                			rule.setComputed_confidence(-1.);
	                			rule.setHuman_confidence(-1.);
	                			rule.setKnowledge_base("dbpedia");
	                			rule.setPremise_triples(premise_triples);
	                			Atom conclusion = new Atom("subject", predicate, "object");
	                    		rule.setConclusion_triple(conclusion);
	                    		rule.setConclusion(conclusion.toString());

	        					Rule check_exist = ruleDAL.getRuleByHashcode(rule.hashCode());
	        					if (check_exist != null) {
	        						//rule exist
	        						count_existing++;
	        					} else {	        
	        						rule.setHashcode(rule.hashCode());
	        						ruleDAL.saveRule(rule);
	        						count_success++;
	        					}
		    	    		} else {
		    	    			output_flag = false;
		    	    		}
		    			} else {
		    				output_flag = false;
		    			}
		    			
		    		}
				}
	    	
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
      
        model.addAttribute("msg", "Import " + count_success + " rules, ignore " + count_existing + " rules");
        return "rule/info";
        
    }
    
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(path = "/rule/import_neg_from_alldbpedia", method = RequestMethod.GET)
    public String importAllDbpedia(@Value("${app.alldbpedia_neg}") String path, ModelMap model) throws JsonIOException, IOException {
    	BufferedReader reader;
    	String predicate = "";
		Boolean output_flag = false;
		Boolean type = false;
		int count_existing = 0;
		int count_success = 0;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			
			while (line != null) {
				line = line.trim();
				if (line.length() > 0) {
					String predicate_pattern_str = "-------------------------\\[(\\D+)\\]";
		    		Pattern predicate_pattern = Pattern.compile(predicate_pattern_str);
		    		Matcher predicate_matcher = predicate_pattern.matcher(line);
		    		
		    		if(predicate_matcher.find()) {
		    			// new predicate
		    			predicate = predicate_matcher.group(1);
		    		} else {
		    			predicate_pattern_str = "-------------------------(\\D+)_\\d+";
			    		predicate_pattern = Pattern.compile(predicate_pattern_str);
			    		predicate_matcher = predicate_pattern.matcher(line);
			    		
			    		if(predicate_matcher.find()) {
			    			// new predicate
			    			predicate = predicate_matcher.group(1);
			    		} else {
			    			if (line.endsWith("--??FILLUP"))
			    				line = line.replace("--??FILLUP", "");
			    			
			    			// check if it's output
			    			if (line.equals("Output rules:")) {
			    				// multilines
			    				output_flag = true;
			    			} else if (line.startsWith("Output rules:")) {
			    				// one line
			    				output_flag = false;
			    				
			    				String output_pattern_str = "\\[((.+)\\((.+)\\,(.+)\\))\\]";
					    		Pattern output_pattern = Pattern.compile(output_pattern_str);
					    		Matcher output_matcher = output_pattern.matcher(line);
					    		
					    		if(output_matcher.find()) {
					    			// new predicate
					    			String output = output_matcher.group(1);
					    			// get rules
				    				String[] rules = output.split(",(?=(\\s|http://dbpedia.org/ontology/))");
				    				for(String premise : rules) {
				    					// create rule
				    					Set<Atom> premise_triples = Parser.premise_to_atom_list(premise);
				    					
				    					Rule rule = new Rule("rudik");
			                			rule.setPremise(premise);
			                			rule.setPredicate(predicate);
			                			rule.setRule_type(type);
			                			rule.setStatus(true);
			                			rule.setComputed_confidence(-1.);
			                			rule.setHuman_confidence(-1.);
			                			rule.setKnowledge_base("dbpedia");
			                			rule.setPremise_triples(premise_triples);
			                			Atom conclusion = new Atom("subject", predicate, "object");
			                    		rule.setConclusion_triple(conclusion);
			                    		rule.setConclusion(conclusion.toString());

			        					Rule check_exist = ruleDAL.getRuleByHashcode(rule.hashCode());
			        					if (check_exist != null) {
			        						//rule exist
			        						count_existing++;
			        					} else {	        
			        						rule.setHashcode(rule.hashCode());
			        						ruleDAL.saveRule(rule);
			        						count_success++;
			        					}
				    				}
					    		}
			    				
			    				
			    			} else if (output_flag) {
			    				// check if this line is a premise
			    	    		if(Parser.is_premise(line)) {
			    	    			// create a new rule
			    	    			String premise = line;
			    	    			Set<Atom> premise_triples = Parser.premise_to_atom_list(premise);
			    	    			
			    	    			Rule rule = new Rule("rudik");
		                			rule.setPremise(premise);
		                			rule.setPredicate(predicate);
		                			rule.setRule_type(type);
		                			rule.setStatus(true);
		                			rule.setComputed_confidence(-1.);
		                			rule.setHuman_confidence(-1.);
		                			rule.setKnowledge_base("dbpedia");
		                			rule.setPremise_triples(premise_triples);
		                			Atom conclusion = new Atom("subject", predicate, "object");
		                    		rule.setConclusion_triple(conclusion);
		                    		rule.setConclusion(conclusion.toString());

		        					Rule check_exist = ruleDAL.getRuleByHashcode(rule.hashCode());
		        					if (check_exist != null) {
		        						//rule exist
		        						count_existing++;
		        					} else {	        
		        						rule.setHashcode(rule.hashCode());
		        						ruleDAL.saveRule(rule);
		        						count_success++;
		        					}
			    	    		} else {
			    	    			output_flag = false;
			    	    		}
			    			} else {
			    				output_flag = false;
			    			}
			    			
			    		}
		    		}
		    		
				}
	    	
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
      
        model.addAttribute("msg", "Import " + count_success + " rules, ignore " + count_existing + " rules");
        return "rule/info";
        
    }

    
    


}