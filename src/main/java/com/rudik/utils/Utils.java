package com.rudik.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public final class Utils {
	
	public static final String WIKIDATA = "wikidata";
	public static final String DBPEDIA = "dbpedia";
	public static final String YAGO3 = "yago3";
	
	//check if string is in list
//    public static boolean is_in_list(List<String> myList, String stringToCheck){
//        for(String str: myList) {
//            if(stringToCheck.contains(str.trim()))
//                return true;
//        }
//        return false;
//    }
    
//    public static String get_config_from_kb(String kb,
//    		@Value("${app.rudikYagoConfig}") String yagoConfig,
//    		@Value("${app.rudikDbpediaConfig}") String dbpediaConfig,
//    		@Value("${app.rudikWikidataConfig}") String wikidataConfig
//    		) {
//    	String config = dbpediaConfig;
//		if (kb.equals("yago3")) {
//			config = yagoConfig;
//		} else if (kb.equals("wikidata")) {
//			config = wikidataConfig;
//		}
//    	return config;
//    }
    
    public static Map<String, String> getKbs() {
    	Map<String, String> knowledgeBases = new HashMap<String, String>() {{
        	put(Utils.DBPEDIA, "DBpedia");
            put(Utils.YAGO3, "Yago3");
            put(Utils.WIKIDATA, "Wikidata");
        }};
        
        return knowledgeBases;
    }
    
    public static Map<Integer, String> getRuleTypes() {
    	Map<Integer, String> rule_types = new HashMap<Integer, String>() {{
        	put(-1, "--None--");
            put(0, "Negative");
            put(1, "Positive");
        }};
        return rule_types;
    }
}
