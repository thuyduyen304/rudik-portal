package com.rudik.form;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.rudik.model.Atom;

public class AddForm {
	@NotNull
    private String knowledgeBase;
	
	@NotNull
	private String predicate;
	
	private Integer ruleType;
	
	private Double humanConfidence;
	
	private Short qualityEvaluation;
	
	private Double computedConfidence;
	
	@NotNull
	private String premise;
	
	public Double getComputedConfidence() {
		return computedConfidence;
	}
	
	public void setComputedConfidence(Double computed_confidence) {
		computedConfidence = computed_confidence;
	}
	
	public Double getHumanConfidence() {
		return humanConfidence;
	}
	
	public void setHumanConfidence(Double human_confidence) {
		humanConfidence = human_confidence;
	}
	
	public Short getQualityEvaluation() {
		return qualityEvaluation;
	}
	
	public void setHumanConfidenceTo(Short quality_evaluation) {
		qualityEvaluation = quality_evaluation;
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
	
	public void setPremise(String premise) {
		this.premise = premise;
	}
	
	public String getPremise() {
		return premise;
	}

}
