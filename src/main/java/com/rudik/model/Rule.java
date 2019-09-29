package com.rudik.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	private String knowledge_base;
	
	@Field("sparql_endpoint")
	private String sparql_endpoint;
	
	@Field("graph_iri")
	private String graph_iri;
	
	@Field("rule_type")
	private Boolean rule_type;
	
	@Field("premise")
	private String premise;
	
	@Field("conclusion")
	private String conclusion;
	
	@Field("hashcode")
	private Integer hashcode;
	
	@Field("premise_triples")
	private Set<Atom> premise_triples;
	
	@Field("conclusion_triple")
	private Atom conclusion_triple;
	
	@Field("human_confidence")
	private Double human_confidence;
	
	@Field("configuration")
	private List<Configuration> configuration;
	
	@Field("computed_confidence")
	private Double computed_confidence;
	
	@Field("quality_evaluation")
	private Short quality_evaluation;
	
	@Field("source")
	private String source;
	
	@Field("status")
	private Boolean status;
	
	public Rule() {
		
	}
	
	public Rule(String source) {
		this.source = source;
	}
	
	public void setStatus(Boolean status) {
		this.status = status;
	}
	
	public Boolean getStatus() {
		return this.status;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public void setSparql_endpoint(String endpoint) {
		sparql_endpoint = endpoint;
	}
	
	public String getSparql_endpoint() {
		return sparql_endpoint;
	}
	
	public void setGraph_iri(String graph_iri) {
		this.graph_iri = graph_iri;
	}
	
	public String getGraph_iri() {
		return graph_iri;
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
	
	public void setPremise_triples(Set<Atom> premise_triples) {
		this.premise_triples = premise_triples;
	}
	
	public Set<Atom> getPremise_triples() {
		return premise_triples;
	}
	
	public void setConclusion_triple(Atom conclusion) {
		this.conclusion_triple = conclusion;
	}
	
	public Atom getConclusionTriple() {
		return conclusion_triple;
	}
	
	public void setHuman_confidence(Double human_confidence) {
		this.human_confidence = human_confidence;
	}
	
	public Double getHuman_confidence() {
		return human_confidence;
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
    
    public String getKnowledge_base() {
    	return knowledge_base;
    }
    
    public void setKnowledge_base(String knowledge_base) {
    	this.knowledge_base = knowledge_base;
    }
    
    public Boolean getRule_type() {
    	return rule_type;
    }
    
    public void setRule_type(Boolean rule_type) {
    	this.rule_type = rule_type;
    }
    
    public String getPremise() {
    	return premise;
    }
    
    public void setPremise(String premise) {
    	this.premise = premise;
    }
    
    public void setComputed_confidence(Double computed_confidence) {
		this.computed_confidence = computed_confidence;
	}
	
	public Double getComputed_confidence() {
		return computed_confidence;
	}
	
	public void setQuality_evaluation(Short quality_evaluation) {
		this.quality_evaluation = quality_evaluation;
	}
	
	public Short getQuality_evaluation() {
		return quality_evaluation;
	}

    @Override
    public String toString() 
    { 
        return "Rule [predicate="
            + predicate 
            + ", premise="
            + premise 
            + ", qualityEvaluation="
            + quality_evaluation + "]"; 
    } 
    
    @Override
    public int hashCode() {
    	final int prime = 31;
		int result = 1;
//		result = prime * result + ((knowledgeBase == null) ? 0 : knowledgeBase.hashCode());
		result = prime * result + ((rule_type == null) ? 0 : rule_type.hashCode());
		result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result + ((premise_triples == null || premise_triples.size() == 0) ? 0 : premise_triples.hashCode());

		return result;
    }
}
