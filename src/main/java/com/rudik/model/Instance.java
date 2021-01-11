package com.rudik.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="sample_instances")
public class Instance {
	@Id
    private String instanceId;
	
	@Field("rule_id")
	private String ruleId;
	
	@Field("predicate")
	private String predicate;
	
	@Field("premise")
	private String premise;
	
	@Field("conclusion")
	private String conclusion;
	
	@Field("hashcode")
	private Integer hashcode;
	
	@Field("premise_triples")
	private Set<Atom> premiseTriples;
	
	@Field("conclusion_triple")
	private Atom conclusionTriple;
	
	@Field("label")
	private Short label;
	
	@Field("details")
	private String details;
	
	@Field("rule_type")
	private Boolean ruleType;
	
	public void setRuleType(Boolean type) {
		ruleType = type;
	}
	
	public Boolean getRuleType() {
		return ruleType;
	}
	
	public void setInstanceId(String instance_id) {
		instanceId = instance_id;
	}
	
	public String getInstanceId() {
		return instanceId;
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
	
	public void setPremiseTriples(Set<Atom> premise_triples) {
		premiseTriples = premise_triples;
	}
	
	public Set<Atom> getPremiseTriples() {
		return premiseTriples;
	}
	
	public void setConclusionTriple(Atom conclusion) {
		this.conclusionTriple = conclusion;
	}
	
	public Atom getConclusionTriple() {
		return conclusionTriple;
	}
	
	public void setLabel(Short label) {
		this.label = label;
	}
	
	public Short getLabel() {
		return label;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public String getDetails() {
		return details;
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
    
    public String getPremise() {
    	return premise;
    }
    
    public void setPremise(String premise) {
    	this.premise = premise;
    }
    
    @Override
    public int hashCode() {
    	final int prime = 31;
		int result = 1;
		result = prime * result + ((predicate == null) ? 0 : predicate.toLowerCase().hashCode());
		result = prime * result + ((premiseTriples == null || premiseTriples.size() == 0) ? 0 : premiseTriples.hashCode());

		return result;
    }

}
