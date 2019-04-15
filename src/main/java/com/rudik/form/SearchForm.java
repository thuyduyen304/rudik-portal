package com.rudik.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SearchForm {
	@NotNull
    private String knowledgeBase;
	
	@NotNull
	private String predicate;
	
	private Integer ruleType;
	
	private Double humanConfidenceFrom;
	
	private Double humanConfidenceTo;
	
	public Double getHumanConfidenceFrom() {
		return humanConfidenceFrom;
	}
	
	public void setHumanConfidenceFrom(Double human_confidence) {
		humanConfidenceFrom = human_confidence;
	}
	
	public Double getHumanConfidenceTo() {
		return humanConfidenceTo;
	}
	
	public void setHumanConfidenceTo(Double human_confidence) {
		humanConfidenceTo = human_confidence;
	}
	
	public String getKnowledgeBase() {
		return knowledgeBase;
	}
	
	public void setKnowledgeBase(String knowledge_base) {
		knowledgeBase = knowledge_base;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public void setRuleType(Integer type) {
		this.ruleType = type;
	}
	
	public Integer getRuleType() {
		return ruleType;
	}

}
