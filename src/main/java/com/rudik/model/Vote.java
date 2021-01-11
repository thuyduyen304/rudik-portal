package com.rudik.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "votes")
public class Vote
{
  @Id
  private String voteId;
  private String ip;
  private String ruleId;
  private String field;
  private Float rating;
  
  public String getRuleId() { return this.ruleId; }

  public void setRuleId(String rid) { this.ruleId = rid; }

  public String getIp() { return this.ip; }

  public void setIp(String ip) { this.ip = ip; }

  public Float getRating() { return this.rating; }

  public void setRating(Float rating) { this.rating = rating; }

  public String getField() { return this.field; }

  public void setField(String f) { this.field = f; }
}
