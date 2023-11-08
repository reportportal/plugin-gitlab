package com.epam.reportportal.extension.gitlab.dto;

public class IssueDto {

  private Long iid;
  private String title;
  private String state;
  private String webUrl;

  public IssueDto() {
  }

  public Long getIid() {
    return iid;
  }

  public void setIid(Long iid) {
    this.iid = iid;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getWebUrl() {
    return webUrl;
  }

  public void setWebUrl(String webUrl) {
    this.webUrl = webUrl;
  }
}
