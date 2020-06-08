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
	
	public Atom() {
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
	
	@Override
    public String toString() 
    { 
        return predicate + "(" + subject + "," + object + ")"; 
    } 
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.toLowerCase().hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.toLowerCase().hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.toLowerCase().hashCode());
		return result;
	}
}
