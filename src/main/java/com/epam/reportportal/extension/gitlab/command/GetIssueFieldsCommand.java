/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.extension.gitlab.command;

import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueFieldsCommand extends ProjectMemberCommand<List<PostFormField>> {

  public GetIssueFieldsCommand(ProjectRepository projectRepository) {
    super(projectRepository);
  }

  @Override
  protected List<PostFormField> invokeCommand(Integration integration, Map<String, Object> params) {
    return List.of(
        PostFormField.builder().id("title").fieldName("title").fieldType("string").isRequired(true)
            .build(),
        PostFormField.builder().id("description").fieldName("Description").fieldType("string")
            .build(),
        PostFormField.builder().id("issue_type").fieldName("Issue type").fieldType("issuetype")
            .isRequired(true)
            .definedValues(List.of(
                new AllowedValue("issue", "issue"),
                new AllowedValue("incident", "incident")))
            .build(),
        PostFormField.builder().id("confidential").fieldName("Confidential").fieldType("string")
            .definedValues(List.of(
                new AllowedValue("false", "No"),
                new AllowedValue("true", "Yes")))
            .build(),
        PostFormField.builder().id("assignee_id").fieldName("Assignee").fieldType("autocomplete")
            .commandName("searchUsers").build(),
        PostFormField.builder().id("due_date").fieldName("Due Date").fieldType("string").build(),
        PostFormField.builder().id("labels").fieldName("Labels").fieldType("multiAutocomplete")
            .commandName("searchLabels").build(),
        PostFormField.builder().id("milestone_id").fieldName("Milestone").fieldType("autocomplete")
            .commandName("searchMilestones").build(),
        PostFormField.builder().id("epic_id").fieldName("Epic").fieldType("autocomplete")
            .commandName("searchEpics").build(),
        PostFormField.builder().id("weight").fieldName("Weight").fieldType("integer").build(),
        PostFormField.builder().id("assignee_ids").fieldName("Assignees")
            .fieldType("multiAutocomplete").commandName("searchUsers").build()
    );
  }

  @Override
  public String getName() {
    return "getIssueFields";
  }
}
