package com.rudik.model;

public class Configuration {
	private Double alpha;
	private Double beta;
	private Double gamma;
	private Integer max_rule_length;
	private Integer nb_negative_examples;
	private Integer nb_positive_examples;
    

    public Configuration() {}

    public Configuration(Double alpha, Double beta, Double gamma, Integer max_rule_length, 
    		Integer nb_negative_examples, Integer nb_positive_examples) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.max_rule_length = max_rule_length;
        this.nb_negative_examples = nb_negative_examples;
        this.nb_positive_examples = nb_positive_examples;
    }
    
    public void setAlpha(Double alpha) {
    	this.alpha = alpha;
    }
    
    public Double getAlpha() {
    	return alpha;
    }
    
    public void setBeta(Double beta) {
    	this.beta = beta;
    }
    
    public Double getBeta() {
    	return beta;
    }
    
    public void setGamma(Double gamma) {
    	this.gamma = gamma;
    }
    
    public Double getGamma() {
    	return gamma;
    }
    
    public void setMax_rule_length(Integer rule_length) {
    	max_rule_length = rule_length;
    }
    
    public Integer getMax_rule_length() {
    	return max_rule_length;
    }
    
    public void setNb_negative_examples(Integer neg_examples) {
    	nb_negative_examples = neg_examples;
    }
    
    public Integer getNb_negative_examples() {
    	return nb_negative_examples;
    }
    
    public void setNb_positive_examples(Integer pos_examples) {
    	nb_positive_examples = pos_examples;
    }
    
    public Integer getNb_positive_examples() {
    	return nb_positive_examples;
    }


    @Override
    public String toString() {
        return String.format(
                "Configuration[alpha=%s, beta='%s', gamma='%s, max_rule_length=%s, nb_negative_examples=%s, nb_positive_examples=%s']",
                alpha, beta, gamma, max_rule_length, nb_negative_examples, nb_positive_examples);
    }
}
