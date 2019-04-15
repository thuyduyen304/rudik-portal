package com.rudik.model;

public class Atom {
	private String subject;
	private String object;
	private String predicate;
	
	public Atom(String subject, String predicate, String object) {
		this.subject = subject;
		this.object = object;
		this.predicate = predicate;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public void setObject(String object) {
		this.object = object;
	}
	
	public String getObject() {
		return this.object;
	}
	
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String getPredicate() {
		return predicate;
	}
}
