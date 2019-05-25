package com.rudik.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.rudik.model.Atom;
import com.rudik.model.Rule;

public final class Parser {
	public static Map<String,String> PREFIX = new HashMap<String, String>() {{
    	put("dbo:", "http://dbpedia.org/ontology/");
    }};
    
    /**
     * Convert a amie rule string into rule object
     * amie rule format: ?b  <dbo:country>  ?f  ?a  <dbo:countryOrigin>  ?f   => ?a  <dbo:launchSite>  ?b
     * @param knowledge_base
     * @param amie_rule
     * @return
     */
	public static Rule amie_to_rudik(String knowledge_base, String amie_rule) throws Exception {
		
    	Rule rule = new Rule("amie");
    	rule.setKnowledgeBase(knowledge_base);
    	rule.setRuleType(true);
    	
    	Map<String,String> params_mapping = new HashMap<String,String>();
    	
    	for (Map.Entry<String,String> entry : PREFIX.entrySet())  {
    		amie_rule = amie_rule.replaceAll(entry.getKey(), entry.getValue());
    	}
    	
		String sentence_pattern_str = "(.+)=>(.+)";
		Pattern sentence_pattern = Pattern.compile(sentence_pattern_str);
//		#0	?b  <dbo:country>  ?f  ?a  <dbo:countryOrigin>  ?f   => ?a  <dbo:launchSite>  ?b
//		#1	?b  <dbo:country>  ?f  ?a  <dbo:countryOrigin>  ?f   
//		#2	 ?a  <dbo:launchSite>  ?b
		
		String atom_pattern_str = "(\\?\\w+)\\s+<([^>]+)>\\s+(\\?\\w+)";
        Pattern atom_pattern = Pattern.compile(atom_pattern_str);
//		#0	?a  <dbo:launchSite>  ?b
//		#1	?a
//		#2	<dbo:launchSite>
//		#3	?b
        
        Matcher sentence_matcher = sentence_pattern.matcher(amie_rule);

        if(sentence_matcher.find()) {
        	String premise_amie = sentence_matcher.group(1);
        	String conclusion_amie = sentence_matcher.group(2);
        	
        	Matcher premise_matcher = atom_pattern.matcher(premise_amie);
        	Matcher conclusion_matcher = atom_pattern.matcher(conclusion_amie);

        	if(conclusion_matcher.find()) {
        		String sbj = conclusion_matcher.group(1);
        		String obj = conclusion_matcher.group(3);
        		String predicate = conclusion_matcher.group(2);
        		
        		params_mapping.put(sbj, "subject");
        		params_mapping.put(obj, "object");
        		
        		rule.setPredicate(predicate.trim());
        		Atom conclusion = new Atom(params_mapping.get(sbj), predicate, params_mapping.get(obj));
        		rule.setConclusionTriple(conclusion);
        		rule.setConclusion(conclusion.toString());
        	}
        	
        	int i = 0;
        	List<Atom> atoms_premise = new ArrayList<Atom>();
        	String premise = "";
        	while(premise_matcher.find()) {
        		String atom_sbj = premise_matcher.group(1);
        		String atom_obj = premise_matcher.group(3);
        		String atom_predicate = premise_matcher.group(2);
        		
        		if(!params_mapping.containsKey(atom_sbj)) {
        			params_mapping.put(atom_sbj, ("v" + i));
        			i++;
        		}
        		if(!params_mapping.containsKey(atom_obj)) {
        			params_mapping.put(atom_obj, ("v" + i));
        			i++;
        		}
        		
        		Atom a = new Atom(params_mapping.get(atom_sbj), atom_predicate, params_mapping.get(atom_obj));
        		atoms_premise.add(a);
        		premise += a.toString() + " & ";
        	}
        	rule.setPremiseTriples(atoms_premise);
        	premise = premise.substring(0, premise.lastIndexOf("&"));
        	rule.setPremise(premise.trim());
        }
        
        rule.setHashcode(rule.getHashcode());
		
		return rule;
		
	}
	
	public static List<Atom> premise_to_atom_list(String premise) throws Exception {
		
    	List<Atom> atoms_premise = new ArrayList<Atom>();
    	for (String atom_str : premise.split("&")) {
    		atom_str = atom_str.trim();
    		String atom_pattern_str = "(.+)\\((.+)\\,(.+)\\)";
    		Pattern atom_pattern = Pattern.compile(atom_pattern_str);
    		Matcher atom_matcher = atom_pattern.matcher(atom_str);
    		
    		if(atom_matcher.find()) {
    			Atom a = new Atom(atom_matcher.group(2), atom_matcher.group(1), atom_matcher.group(3));
        		atoms_premise.add(a);
    		} 
    	}
		
		return atoms_premise;
		
	}
}
