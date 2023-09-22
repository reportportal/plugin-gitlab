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
import com.epam.reportportal.extension.gitlab.command.utils.GitlabProperties;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClient;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClientProvider;
import com.epam.reportportal.extension.gitlab.rest.client.model.IssueExtended;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.hibernate.bytecode.BytecodeLogger.LOGGER;

/**
 * @author Zsolt Nagyaghy
 */
public class GetIssueTypesCommand extends ProjectManagerCommand<List<String>> {

    private final GitlabClientProvider gitlabClientProvider;

    public GetIssueTypesCommand(ProjectRepository projectRepository, GitlabClientProvider gitlabClientProvider) {
        super(projectRepository);
        this.gitlabClientProvider = gitlabClientProvider;
    }

    @Override
    public String getName() {
        return "getIssueTypes";
    }

    @Override
    protected List<String> invokeCommand(Integration integration, Map<String, Object> params) {
        return List.of("Issue", "Incident", "test_case");
    }
}
