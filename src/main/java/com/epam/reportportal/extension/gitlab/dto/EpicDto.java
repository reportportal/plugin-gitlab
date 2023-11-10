package com.epam.reportportal.extension.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EpicDto {

  private Long id;
  private String title;

  public EpicDto() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @JsonProperty("name")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

}
