package com.epam.reportportal.extension.gitlab.rest.client.model;

public class IssueExtended extends org.gitlab4j.api.models.Issue {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
