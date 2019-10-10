package com.rudik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;

import com.rudik.dal.*;
import com.rudik.form.AddForm;
import com.rudik.form.SearchForm;
import com.rudik.model.Atom;
import com.rudik.model.Instance;
import com.rudik.model.Rule;
import com.rudik.utils.Parser;
import com.rudik.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

@Controller
public class RuleController {
	@Autowired
	private RuleDAL ruleDAL;
	private InstanceDAL instanceDAL;
	
	@Autowired
    RuleRepository ruleRepository;
    
    @GetMapping(value = {"/", "/rule/search"})
    public String showSearchForm(Model model) {
    	Map<String, String> knowledgeBases = Utils.get_kbs();
        
    	model.addAttribute("searchForm", new SearchForm());
        model.addAttribute("knowledgeBases", knowledgeBases);

        Map<Integer, String> ruleTypes = new HashMap<Integer, String>() {{
        	put(-1, "--None--");
            put(0, "Negative");
            put(1, "Positive");
        }};
        model.addAttribute("ruleTypes", ruleTypes);
        return "rule/searchForm";
    }
    
    @PostMapping(value = "/rule/search")
    public String submitSearchForm(@Valid @ModelAttribute("searchForm")SearchForm searchForm, 
    	      BindingResult result, ModelMap model) {
    	ObjectWriter  obj = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	Map<String, Object> criteria = new HashMap<String, Object>();
    	
    	if(searchForm.getRuleType() != -1) {
    		criteria.put("ruleType", (searchForm.getRuleType() == 1));
    	}
    	
        Map<String, String> rules = new HashMap<String, String>();
        
    	model.addAttribute("rules", rules);
    	
        model.addAttribute("knowledgeBases", Utils.get_kbs());
        
        Map<Integer, String> ruleTypes = new HashMap<Integer, String>() {{
        	put(-1, "--None--");
            put(0, "Negative");
            put(1, "Positive");
        }};
        model.addAttribute("ruleTypes", ruleTypes);

        return "rule/searchForm";
    }
    
    @GetMapping(value = "/rule/add")
    public String showAddForm(Model model) {
    	model.addAttribute("addForm", new Rule());
    	
    	model.addAttribute("knowledgeBases", Utils.get_kbs());
    	
    	Map<Integer, String> ruleTypes = new HashMap<Integer, String>() {{
      	put(-1, "--None--");
          put(0, "Negative");
          put(1, "Positive");
      }};
      model.addAttribute("ruleTypes", ruleTypes);
      
    	return "rule/addForm";
    }
    
    @Secured({"ROLE_ADMIN"})
    @GetMapping(value = "/rule/approve")
    public String approveForm(Model model) {
    	model.addAttribute("searchForm", new SearchForm());
    	model.addAttribute("knowledgeBases", Utils.get_kbs());

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
      
    	return "rule/approveRules";
    }
    
    @Secured({"ROLE_ADMIN"})
    @PostMapping(value = "/rule/approve")
    public String submitApprove(@Valid @ModelAttribute("searchForm")SearchForm searchForm, 
    	      BindingResult result, ModelMap model) {
    	ObjectWriter  obj = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	Map<String, Object> criteria = new HashMap<String, Object>();
    	
    	if(searchForm.getRuleType() != -1) {
    		criteria.put("ruleType", (searchForm.getRuleType() == 1));
    	}
    	
        Map<String, String> rules = new HashMap<String, String>();
        
    	model.addAttribute("rules", rules);
    	
        model.addAttribute("knowledgeBases", Utils.get_kbs());
        
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
        return "rule/approveRules";
    }
    
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(path = "/rule/export_all", method = RequestMethod.GET)
    public  ResponseEntity<Resource> exportAllRules(@Value("${app.exportPath}") String directory) throws JsonIOException, IOException {
    	GsonBuilder builder = new GsonBuilder();
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

	        					List<Rule> check_exist = ruleRepository.findByHashcode(rule.hashCode());
	        					if (check_exist.size() > 0) {
	        						//rule exist
	        						count_existing++;
	        					} else {	        
	        						rule.setHashcode(rule.hashCode());
	        						ruleRepository.save(rule);
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

		        					List<Rule> check_exist = ruleRepository.findByHashcode(rule.hashCode());
		        					if (check_exist.size() > 0) {
		        						//rule exist
		        						count_existing++;
		        					} else {	        
		        						rule.setHashcode(rule.hashCode());
		        						ruleRepository.save(rule);
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

    @Secured({"ROLE_ADMIN"})
    @GetMapping(value = "/rule_sample/{id}")
    public String sample_instances(Model model, @PathVariable(value="id") String rule_id) {
    	Rule rule = ruleDAL.getRuleById(rule_id);
    	model.addAttribute("rule", rule);
    	
    	return "rule/sampleInstances";
    } 

}