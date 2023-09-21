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

import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.reportportal.extension.gitlab.command.utils.GitlabMapper;
import com.epam.reportportal.extension.gitlab.command.utils.GitlabProperties;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClientProvider;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.epam.reportportal.extension.util.RequestEntityValidator;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class PostTicketCommand extends ProjectMemberCommand<Ticket> {

    private final RequestEntityConverter requestEntityConverter;

    private final GitlabClientProvider gitlabClientProvider;

    private final DataStoreService dataStoreService;

    public PostTicketCommand(ProjectRepository projectRepository, RequestEntityConverter requestEntityConverter,
                             GitlabClientProvider gitlabClientProvider,
                             DataStoreService dataStoreService) {
        super(projectRepository);
        this.requestEntityConverter = requestEntityConverter;
        this.gitlabClientProvider = gitlabClientProvider;
        this.dataStoreService = dataStoreService;
    }

    @Override
    protected Ticket invokeCommand(Integration integration, Map<String, Object> params) {
        PostTicketRQ ticketRQ = requestEntityConverter.getEntity(ENTITY_PARAM, params, PostTicketRQ.class);
        RequestEntityValidator.validate(ticketRQ);
        expect(ticketRQ.getFields(), not(isNull())).verify(UNABLE_INTERACT_WITH_INTEGRATION, "External System fields set is empty!");

        String project = GitlabProperties.PROJECT.getParam(integration.getParams())
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project key is not specified."));

        Map<String, String> queryParams = ticketRQ.getFields().stream().collect(Collectors.toMap(PostFormField::getId, field -> field.getValue().get(0)));

        try {
            return GitlabMapper.toTicket(gitlabClientProvider.get(integration.getParams()).postIssue(project, queryParams));
        } catch (Exception e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
        }

    }

    @Override
    public String getName() {
        return "postTicket";
    }
}
