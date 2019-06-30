package com.rudik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import com.rudik.dal.*;
import com.rudik.form.AddForm;
import com.rudik.form.SearchForm;
import com.rudik.model.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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

}