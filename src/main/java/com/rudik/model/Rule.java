package com.rudik.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.rudik.model.Atom;
import com.rudik.model.Configuration;

@Document(collection="rules")
public class Rule {
	@Id
    private String ruleId;
	
	private String predicate;
	
	@Field("knowledge_base")
	private String knowledgeBase;
	
	@Field("sparql_endpoint")
	private String sparqlEndpoint;
	
	@Field("graph_iri")
	private String graphIri;
	
	@Field("rule_type")
	private Boolean ruleType;
	
	@Field("premise")
	private String premise;
	
	@Field("conclusion")
	private String conclusion;
	
	@Field("hashcode")
	private Integer hashcode;
	
	@Field("premise_triples")
	private List<Atom> premiseTriples;
	
	@Field("conclusion_triple")
	private Atom conclusionTriple;
	
	@Field("human_confidence")
	private Double humanConfidence;
	
	@Field("configuration")
	private List<Configuration> configuration;
	
	public void setSparqlEndpoint(String endpoint) {
		sparqlEndpoint = endpoint;
	}
	
	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}
	
	public void setGraphIri(String graph_iri) {
		graphIri = graph_iri;
	}
	
	public String getGraphIri() {
		return graphIri;
	}
	
	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}
	
	public String getConclusion() {
		return conclusion;
	}
	
	public void setHashcode(Integer code) {
		hashcode = code;
	}
	
	public Integer getHashcode() {
		return hashcode;
	}
	
	public void setPremiseTriples(List<Atom> premise_triples) {
		premiseTriples = premise_triples;
	}
	
	public List<Atom> getPremiseTriples() {
		return premiseTriples;
	}
	
	public void setConclusionTriple(Atom conclusion) {
		this.conclusionTriple = conclusion;
	}
	
	public Atom getConclusionTriple() {
		return conclusionTriple;
	}
	
	public void setHumanConfidence(Double human_confidence) {
		this.humanConfidence = human_confidence;
	}
	
	public Double getHumanConfidence() {
		return humanConfidence;
	}
	
	public void setConfiguration(List<Configuration> config) {
		this.configuration = config;
	}
	
	public List<Configuration> getConfiguration() {
		return configuration;
	}
	
	public String getRuleId() {
    	return ruleId;
    }
    
    public void setRuleId(String ruleId) {
    	this.ruleId = ruleId;
    }
    
    public String getPredicate() {
    	return predicate;
    }
    
    public void setPredicate(String predicate) {
    	this.predicate = predicate;
    }
    
    public String getKnowledgeBase() {
    	return knowledgeBase;
    }
    
    public void setKnowledgeBase(String knowledge_base) {
    	this.knowledgeBase = knowledge_base;
    }
    
    public Boolean getRuleType() {
    	return ruleType;
    }
    
    public void setRuleType(Boolean rule_type) {
    	this.ruleType = rule_type;
    }
    
    public String getPremise() {
    	return premise;
    }
    
    public void setPremise(String premise) {
    	this.premise = premise;
    }

    @Override
    public String toString() 
    { 
        return "Rule [predicate="
            + predicate 
            + ", premise="
            + premise 
            + ", rule_type="
            + ruleType + "]"; 
    } 
}
