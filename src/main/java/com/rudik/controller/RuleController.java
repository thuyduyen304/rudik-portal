package com.rudik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.rudik.model.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

@Controller
public class RuleController {
	@Autowired
	private RuleDAL ruleDAL;
    
    @GetMapping(value = {"/", "/rule/search"})
    public String showSearchForm(Model model) {
    	Map<String, String> knowledgeBases = new HashMap<String, String>() {{
        	put("dbpedia", "DBpedia");
//            put("yago", "Yago");
//            put("wikidata", "Wikidata");
        }};
        
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
    	
        model.addAttribute("knowledgeBases", ruleDAL.getAllKnowledgeBase());
        
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
    	model.addAttribute("addForm", new AddForm());
    	model.addAttribute("knowledgeBases", ruleDAL.getAllKnowledgeBase());
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
    	Map<String, String> knowledgeBases = new HashMap<String, String>() {{
        	put("dbpedia", "DBpedia");
//            put("yago", "Yago");
//            put("wikidata", "Wikidata");
        }};
    	model.addAttribute("searchForm", new SearchForm());
    	model.addAttribute("knowledgeBases", knowledgeBases);

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
    	
        model.addAttribute("knowledgeBases", ruleDAL.getAllKnowledgeBase());
        
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

}