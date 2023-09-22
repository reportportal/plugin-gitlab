/*
 * Copyright 2021 EPAM Systems
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

import com.epam.reportportal.extension.ProjectManagerCommand;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;

import java.util.List;
import java.util.Map;

/**
 * @author Zsolt Nagyaghy
 */
public class GetIssueFieldsCommand extends ProjectManagerCommand<List<PostFormField>> {

    public GetIssueFieldsCommand(ProjectRepository projectRepository) {
        super(projectRepository);
    }

    @Override
    public String getName() {
        return "getIssueFields";
    }

    @Override
    protected List<PostFormField> invokeCommand(Integration integration, Map<String, Object> params) {
        return List.of(
                new PostFormField("title", "Title", "string", true, null, null),
                new PostFormField("description", "Description", "string", false, null, null),
                new PostFormField("issue_type", "Issue type", "string", false, null, List.of(
                        new AllowedValue("issue", "Issue"),
                        new AllowedValue("incident", "Incident"),
                        new AllowedValue("test_case", "Test Case"))),
                new PostFormField("confidential", "Confidential", "boolean", false, null, null),
                new PostFormField("assignee_ids", "Assignee ID", "array", false, null, null),
                new PostFormField("milestone_id", "Milestone ID", "string", false, null, null),
                new PostFormField("epic_id", "Epic ID", "integer", false, null, null),
                new PostFormField("labels", "Labels", "string", false, null, null),
                new PostFormField("created_at", "Assignee ID", "string", false, null, null),
                new PostFormField("due_date", "Due Date", "string", false, null, null),
                new PostFormField("merge_request_to_resolve_discussions_of", "IID of a merge request ", "integer", false, null, null),
                new PostFormField("discussion_to_resolve", "ID of discussion to resolve", "string", false, null, null)
        );
    }
}
